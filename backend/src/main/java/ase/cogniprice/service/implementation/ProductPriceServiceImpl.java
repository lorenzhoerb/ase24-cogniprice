package ase.cogniprice.service.implementation;

import ase.cogniprice.controller.dto.store.product.PriceDto;
import ase.cogniprice.entity.ProductPrice;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.InvalidProductPriceException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.repository.ProductPriceRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.ProductPriceService;
import org.javamoney.moneta.Money;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class ProductPriceServiceImpl implements ProductPriceService {

    private final StoreProductRepository storeProductRepository;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;

    public ProductPriceServiceImpl(
            StoreProductRepository storeProductRepository,
            ProductPriceRepository productPriceRepository,
            ProductRepository productRepository) {
        this.storeProductRepository = storeProductRepository;
        this.productPriceRepository = productPriceRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    @Override
    public void addPrice(StoreProduct.StoreProductId storeProductId, Money price, LocalDateTime timestamp) {
        validateInput(storeProductId, price, timestamp);

        StoreProduct storeProduct = storeProductRepository.findById(storeProductId)
                .orElseThrow(() -> new NotFoundException("StoreProduct not found for ID: " + storeProductId));

        if (timestamp.isAfter(LocalDateTime.now())) {
            throw new InvalidProductPriceException("Timestamp cannot be in the future.");
        }

        // Create and populate ProductPrice entity
        ProductPrice productPrice = new ProductPrice();
        productPrice.setStoreProduct(storeProduct);
        productPrice.setId(new ProductPrice.ProductPriceId(storeProductId, timestamp));
        productPrice.setPrice(price);

        productPriceRepository.save(productPrice);
    }

    private void validateInput(StoreProduct.StoreProductId storeProductId, Money price, LocalDateTime timestamp) {
        if (storeProductId == null) {
            throw new IllegalArgumentException("StoreProductId must not be null.");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price must not be null.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp must not be null.");
        }
        if (price.getNumber().doubleValueExact() < 0) {
            throw new InvalidProductPriceException("Price must be greater than or equal to 0.");
        }
    }

    @Override
    public List<PriceDto> getProductPricesByProductIt(Long productId) throws EntityNotFoundException {
        LOG.trace("Fetching price trends for productId {}", productId);
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product with ID " + productId + " does not exist.");
        }
        return productPriceRepository.findProductPricesByProductId(productId);
    }
}
