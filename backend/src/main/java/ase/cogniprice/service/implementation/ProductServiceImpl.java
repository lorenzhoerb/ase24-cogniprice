package ase.cogniprice.service.implementation;


import ase.cogniprice.controller.dto.competitor.CompetitorDetailsDto;
import ase.cogniprice.controller.dto.product.ProductBatchImporterDto;
import ase.cogniprice.controller.dto.product.ProductCreateDto;
import ase.cogniprice.controller.dto.product.ProductDetailsDto;
import ase.cogniprice.controller.dto.product.ProductDetailsWithCompetitorUrlDto;
import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.controller.dto.product.ProductUpdateDto;
import ase.cogniprice.controller.dto.store.product.PriceDto;
import ase.cogniprice.controller.dto.store.product.StoreProductCreateDto;
import ase.cogniprice.controller.mapper.ProductMapper;
import ase.cogniprice.entity.Product;
import ase.cogniprice.exception.DomainValidationException;
import ase.cogniprice.exception.GtinLookupException;
import ase.cogniprice.exception.StoreProductAlreadyExistsException;
import ase.cogniprice.exception.UrlNotReachableException;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.service.ApplicationUserService;
import ase.cogniprice.service.CompetitorService;
import ase.cogniprice.service.ProductService;
import ase.cogniprice.service.StoreProductService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RestTemplate restTemplate;
    private final ProductRepository productRepository;
    private final StoreProductService storeProductService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductMapper productMapper;
    private final CompetitorService competitorService;
    private final Environment env;

    @Autowired
    public ProductServiceImpl(RestTemplate restTemplate,
                              ProductRepository productRepository,
                              StoreProductService storeProductService,
                              CompetitorService competitorService,
                              ProductCategoryRepository productCategoryRepository,
                              ProductMapper productMapper,
                              Environment env) {
        this.restTemplate = restTemplate;
        this.productRepository = productRepository;
        this.storeProductService = storeProductService;
        this.productCategoryRepository = productCategoryRepository;
        this.productMapper = productMapper;
        this.competitorService = competitorService;
        this.env = env;
    }

    @Override
    public Product createProduct(ProductCreateDto productCreateDto, MultipartFile image) throws GtinLookupException {
        LOG.trace("createProduct");
        String gtin = productCreateDto.getGtin();

        // Check if the product already exists in the database
        Product existingProduct = this.productRepository.findProductByGtin(gtin);

        if (existingProduct != null) {
            // Product exists, return it to be handled by the controller
            return existingProduct;
        }


        Product product = this.productMapper.toEntityWithoutImage(productCreateDto, productCategoryRepository);
        this.productRepository.save(product);
        this.productMapper.mapProductImageIntoEntity(product, image);
        Product createdProduct = this.productRepository.save(product);


        return createdProduct; // return product
    }

    @Override
    @Transactional
    public void createBatchProducts(List<ProductBatchImporterDto> productBatchDtos, String username) throws GtinLookupException, EntityNotFoundException, StoreProductAlreadyExistsException,
        DomainValidationException, UrlNotReachableException {
        LOG.trace("createBatchProducts");

        for (ProductBatchImporterDto productBatchDto : productBatchDtos) {
            String gtin = productBatchDto.getGtin();
            Product existingProduct = this.productRepository.findProductByGtin(gtin);

            if (existingProduct != null) {
                continue;
            }

            Product product = this.productMapper.toEntityWithoutImage(productBatchDto, productCategoryRepository);
            Product createdProduct = this.productRepository.save(product);

            if (productBatchDto.getCompetitor() != null && productBatchDto.getProductUrl() != null) {
                List<CompetitorDetailsDto> competitors = this.competitorService.getCompetitorsByName(productBatchDto.getCompetitor());

                if (competitors.isEmpty()) {
                    throw new EntityNotFoundException("Competitor not found: " + productBatchDto.getCompetitor());
                }
                Long competitorId = competitors.get(0).getId();

                StoreProductCreateDto storeProductCreateDto = new StoreProductCreateDto();
                storeProductCreateDto.setProductId(createdProduct.getId());
                storeProductCreateDto.setCompetitorId(competitorId);
                storeProductCreateDto.setProductUrl(productBatchDto.getProductUrl());

                this.storeProductService.createStoreProduct(storeProductCreateDto, username);
            }
        }

    }

    @Override
    public Product updateProduct(ProductUpdateDto productUpdateDto) throws EntityNotFoundException, IllegalArgumentException {
        LOG.trace("updateProduct");
        Long productId = productUpdateDto.getId();

        // Fetch the existing product
        Product existingProduct = this.productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product with ID " + productId + " not found."));

        this.productMapper.updateEntity(existingProduct, productUpdateDto, productCategoryRepository);

        Product updatedProduct = this.productRepository.save(existingProduct);

        LOG.debug("Updated product with ID {}", productId);
        return updatedProduct;
    }

    @Override
    @Transactional
    public List<ProductDetailsDto> getProductsOfUser(String username) {
        LOG.trace("getProductsOfUser");
        List<Product> products = productRepository.findProductsByUser(username);

        return products.stream()
            .map(productMapper::productToProductDetailsDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Page<ProductDetailsDto> searchProducts(String query, String category, Pageable pageable) {
        LOG.trace("getProductsOfUser");
        Page<Product> productPage = productRepository.getFilteredProducts(query, category, pageable);

        List<ProductDetailsDto> productDetailsDtos = productPage
            .getContent()
            .stream()
            .map(productMapper::productToProductDetailsDto)
            .collect(Collectors.toList());

        return new PageImpl<>(productDetailsDtos, pageable, productPage.getTotalElements());
    }

    @Override
    @Transactional
    public Page<ProductDetailsWithCompetitorUrlDto> searchProductsByCompetitorId(String query, String category, Pageable pageable, Long competitorId) {
        LOG.trace("getProductsByCompetitorId");
        Page<ProductDetailsWithCompetitorUrlDto> productPage = productRepository.getFilteredProductsByCompetitorId(query, category, pageable, competitorId);

        return productPage;
    }

    @Override
    @Transactional
    public List<ProductDetailsDto> getSuggestions(String query) {
        LOG.trace("getSuggestions");

        Page<Product> productPage = productRepository.getFilteredProducts(query, "", PageRequest.of(0, 10));

        List<ProductDetailsDto> productDetailsDtos = productPage
            .getContent()
            .stream()
            .map(productMapper::productToProductDetailsDto)
            .collect(Collectors.toList());

        return productDetailsDtos;
    }

    @Override
    @Transactional
    public Product getProductByGtin(String gtin) {
        return productRepository.findProductByGtin(gtin);
    }


    @Override
    @Transactional
    public ProductDetailsWithPricesDto getProductWithPriceAndImageById(Long productId) {
        ProductDetailsWithPricesDto productDetails = productRepository.findProductDetailsById(productId);

        if (productDetails == null) {
            throw new EntityNotFoundException("Product not found for ID: " + productId);
        }

        List<Object[]> priceDetails = productRepository.findMinMaxPricesByProductId(productId);

        // Prepare placeholders for the lowest and highest prices
        PriceDto lowestPrice = null;
        PriceDto highestPrice = null;

        // Process price details
        for (Object[] row : priceDetails) {
            Long competitorId = ((Number) row[1]).longValue();
            String competitorName = (String) row[2];
            LocalDateTime priceTime = ((Timestamp) row[3]).toLocalDateTime();
            BigDecimal price = (BigDecimal) row[4];
            String currency = (String) row[5];
            String priceType = (String) row[6]; // 'Max Price' or 'Min Price'

            if ("Min Price".equals(priceType)) {
                lowestPrice = new PriceDto(competitorId, price, competitorName, currency, priceTime);
            } else if ("Max Price".equals(priceType)) {
                highestPrice = new PriceDto(competitorId, price, competitorName, currency, priceTime);
            }
        }

        // Set the prices in the product DTO
        productDetails.setLowestPrice(lowestPrice);
        productDetails.setHighestPrice(highestPrice);

        return productDetails;
    }

    // Account will automatically expire on 12-22-2024
    public void lookupGtin(String gtin) throws GtinLookupException {
        LOG.trace("lookUpGtin");
        String apiKey = env.getProperty("barcode.lookup.api");
        String barcodeUrl = env.getProperty("barcode.lookup.url");
        String url = String.format(barcodeUrl, gtin, apiKey);

        // Send GET request and receive the response
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Check the response status and throw error if NotFound
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new GtinLookupException("Error fetching product info from BarcodeLookup API");
        }
    }
}
