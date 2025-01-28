package ase.cogniprice.service.implementation;

import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.controller.dto.store.product.PriceDto;
import ase.cogniprice.controller.dto.store.product.StoreProductCreateDto;
import ase.cogniprice.controller.mapper.StoreProductMapper;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.DomainValidationException;
import ase.cogniprice.exception.StoreProductAlreadyExistsException;
import ase.cogniprice.exception.UrlNotReachableException;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.StoreProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreProductServiceImpl implements StoreProductService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ProductRepository productRepository;
    private final CompetitorRepository competitorRepository;

    private final StoreProductRepository storeProductRepository;

    private final ApplicationUserRepository applicationUserRepository;

    private final StoreProductMapper storeProductMapper;

    private final RestTemplate restTemplate;

    private final EntityManager entityManager;


    @Autowired
    public StoreProductServiceImpl(CompetitorRepository competitorRepository,
                                   ProductRepository productRepository,
                                   StoreProductRepository storeProductRepository,
                                   ApplicationUserRepository applicationUserRepository,
                                   StoreProductMapper storeProductMapper,
                                   RestTemplate restTemplate,
                                   EntityManager entityManager) {
        this.competitorRepository = competitorRepository;
        this.productRepository = productRepository;
        this.storeProductRepository = storeProductRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.storeProductMapper = storeProductMapper;
        this.restTemplate = restTemplate;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void createStoreProduct(StoreProductCreateDto storeProductCreateDto, String username)
        throws EntityNotFoundException, StoreProductAlreadyExistsException,
        DomainValidationException, UrlNotReachableException {
        LOG.trace("createStoreProduct");

        Long productId = storeProductCreateDto.getProductId();
        Long competitorId = storeProductCreateDto.getCompetitorId();
        StoreProduct.StoreProductId storeProductId = new StoreProduct.StoreProductId(productId, competitorId);

        this.validateStoreProductDoesNotExist(storeProductId);

        StoreProduct storeProduct = this.storeProductMapper.toEntity(storeProductCreateDto,
            this.productRepository,
            this.competitorRepository);

        this.validateStoreProductUrlContainsCompetitorDomain(
            storeProduct.getProductUrl(), storeProduct.getCompetitor().getUrl());

        //this.validateStoreProductUrlIsReachable(storeProduct.getProductUrl());

        this.storeProductRepository.save(storeProduct);

        this.assignStoreProductToUser(storeProduct, username);
    }


    @Override
    public void removeStoreProduct(StoreProduct.StoreProductId storeProductId, String username) throws EntityNotFoundException {
        LOG.trace("removeStoreProduct");

        StoreProduct storeProduct = this.findStoreProduct(storeProductId);
        ApplicationUser applicationUser = this.getApplicationUserByUsername(username);
        this.removeStoreProductFromUser(applicationUser, storeProduct);
    }

    @Override
    @Transactional
    public void addStoreProduct(StoreProduct.StoreProductId storeProductId, String username)
        throws EntityNotFoundException, StoreProductAlreadyExistsException {
        LOG.trace("addStoreProduct");

        ApplicationUser applicationUser = this.getApplicationUserByUsername(username);
        StoreProduct storeProduct = this.findStoreProduct(storeProductId);

        this.addStoreProductToUser(applicationUser, storeProduct);
    }

    @Transactional
    public Page<ProductDetailsWithPricesDto> getStoreProductsWithPriceAndImage(String username, String name, String category, Pageable pageable) {
        LOG.trace("getStoreProductsWithPriceAndImage");

        // Fetch product details
        Page<ProductDetailsWithPricesDto> productPage  = storeProductRepository.findStoreProductsWithImageByUser(username, name, category, pageable);

        // Fetch price details with price type
        List<Object[]> priceDetails = storeProductRepository.getProductPriceMinAndMax();

        // Create a map to store the min and max prices for each product_id
        Map<Long, PriceDto> minPriceMap = new HashMap<>();
        Map<Long, PriceDto> maxPriceMap = new HashMap<>();

        // Iterate through the price details and store the min and max price for each product_id
        for (Object[] row : priceDetails) {
            Long productId = (Long) row[0];
            String competitorName = (String) row[1];
            LocalDateTime priceTime = ((Timestamp) row[2]).toLocalDateTime();
            BigDecimal price = (BigDecimal) row[3];
            String currency = (String) row[4];
            Long competitorId = (Long) row[5];
            String priceType = (String) row[6];  // 'Max Price' or 'Min Price'

            // Check if the current price is the minimum or maximum for the product
            if ("Min Price".equals(priceType)) {
                minPriceMap.put(productId, new PriceDto(competitorId, price, competitorName, currency, priceTime));
            } else if ("Max Price".equals(priceType)) {
                maxPriceMap.put(productId, new PriceDto(competitorId, price, competitorName, currency, priceTime));
            }
        }

        // Combine product and price details
        List<ProductDetailsWithPricesDto> combinedResults = new ArrayList<>();
        for (ProductDetailsWithPricesDto productDto :  productPage.getContent()) {
            Long productId = productDto.getProductId();

            PriceDto minPriceDto = minPriceMap.get(productId);
            PriceDto maxPriceDto = maxPriceMap.get(productId);

            // Set the min and max prices to the product DTO
            productDto.setLowestPrice(minPriceDto);
            productDto.setHighestPrice(maxPriceDto);

            combinedResults.add(productDto);
        }

        return new PageImpl<>(combinedResults, pageable, productPage.getTotalElements());
    }

    @Override
    @Transactional
    public void removeAllStoreProduct(Long productId, String username)
        throws EntityNotFoundException {
        this.applicationUserRepository.deleteProductFromWatchlist(username, productId);
        // Clear the persistence context to force a database load.
        // Faster than manually setting the users storeproduct set(by ~2500ms)
        entityManager.clear();
    }

    private void validateStoreProductUrlContainsCompetitorDomain(String productUrl, String domain) throws DomainValidationException {
        if (!productUrl.contains(domain)) {
            throw new DomainValidationException(
                String.format("The provided product URL '%s' does not contain the required domain '%s'.", productUrl, domain)
            );
        }
    }

    private void validateStoreProductUrlIsReachable(String productUrl)
        throws UrlNotReachableException {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                productUrl,
                HttpMethod.GET,
                null,
                Void.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new UrlNotReachableException("The URL is not reachable. Response code: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            throw new UrlNotReachableException("Client error while reaching URL: " + productUrl, e);
        } catch (RestClientException e) {
            throw new UrlNotReachableException("Error reaching URL: " + productUrl, e);
        }
    }

    private ApplicationUser getApplicationUserByUsername(String username) throws EntityNotFoundException {
        LOG.trace("getApplicationUserByEmail");
        ApplicationUser applicationUser =
            applicationUserRepository.findApplicationUserByUsername(username);
        if (applicationUser != null) {
            return applicationUser;
        } else {
            throw new EntityNotFoundException("User with username '" + username + "' not found");
        }
    }

    private void removeStoreProductFromUser(ApplicationUser applicationUser, StoreProduct storeProduct)
        throws EntityNotFoundException {
        LOG.trace("removeStoreProductFromUser");

        boolean isRemoved = applicationUser.removeStoreProduct(storeProduct);
        if (!isRemoved) {
            throw new EntityNotFoundException(
                String.format("StoreProduct with Product ID %d and Competitor ID %d does not exist with user %s.",
                    storeProduct.getId().getProductId(), storeProduct.getId().getCompetitorId(), applicationUser.getUsername())
            );
        }
        applicationUserRepository.save(applicationUser);
    }

    private void addStoreProductToUser(ApplicationUser applicationUser, StoreProduct storeProduct)
        throws StoreProductAlreadyExistsException {
        LOG.trace("addStoreProductToUser");

        boolean isAdded = applicationUser.addStoreProduct(storeProduct, storeProductRepository);
        if (!isAdded) {
            throw new StoreProductAlreadyExistsException(
                String.format("StoreProduct with Product ID %d and Competitor ID %d does already exist with user %s.",
                    storeProduct.getId().getProductId(), storeProduct.getId().getCompetitorId(), applicationUser.getUsername())
            );
        }
        applicationUserRepository.save(applicationUser);
    }


    private void validateApplicationUserHasStoreProductInWatchlist(String username, StoreProduct.StoreProductId storeProductId) throws EntityNotFoundException {
        LOG.trace("validateApplicationUserHasStoreProductInWatchlist");

        boolean isPresent = this.storeProductRepository.existsStoreProductForUser(username, storeProductId);
        if (!isPresent) {
            throw new EntityNotFoundException(
                String.format("StoreProduct with Product ID %d and Competitor ID %d does not exist with user %s.",
                    storeProductId.getProductId(), storeProductId.getCompetitorId(), username)
            );
        }
    }

    private void validateStoreProductDoesNotExist(StoreProduct.StoreProductId storeProductId)
        throws StoreProductAlreadyExistsException {
        LOG.trace("validateStoreProductDoesNotExist");

        if (storeProductRepository.existsById(storeProductId)) {
            throw new StoreProductAlreadyExistsException(
                String.format("StoreProduct with Product ID %d and Competitor ID %d already exists.",
                    storeProductId.getProductId(), storeProductId.getCompetitorId())
            );
        }
    }

    private StoreProduct findStoreProduct(StoreProduct.StoreProductId storeProductId)
        throws EntityNotFoundException {
        LOG.trace("findStoreProduct");

        return this.storeProductRepository.findById(storeProductId)
            .orElseThrow(() -> new EntityNotFoundException(
                String.format("StoreProduct with Product ID %d and Competitor ID %d does not exist.",
                    storeProductId.getProductId(), storeProductId.getCompetitorId())
            ));
    }


    private void assignStoreProductToUser(StoreProduct storeProduct, String username) {
        LOG.trace("addStoreProductToUser");

        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(username);
        if (applicationUser == null) {
            throw new EntityNotFoundException(String.format("User with username '%s' not found", username));
        }
        applicationUser.addStoreProduct(storeProduct, storeProductRepository);
        applicationUserRepository.save(applicationUser);
    }
}
