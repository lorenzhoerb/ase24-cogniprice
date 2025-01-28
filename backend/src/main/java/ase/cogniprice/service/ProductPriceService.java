package ase.cogniprice.service;

import ase.cogniprice.controller.dto.store.product.PriceDto;
import jakarta.persistence.EntityNotFoundException;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.InvalidProductPriceException;
import ase.cogniprice.exception.NotFoundException;
import org.javamoney.moneta.Money;

import java.util.List;
import java.time.LocalDateTime;

/**
 * Service for managing product prices.
 *
 * @author Lorenz
 */
public interface ProductPriceService {


    /**
     * Adds a new price entry for a specific store product.
     *
     * @param storeProductId the composite ID of the store product, including productId and competitorId.
     * @param price          the price details, including amount, currency, and timestamp.
     * @param timestamp      timestamp of the price
     * @throws NotFoundException            if the store product with the specified ID does not exist.
     * @throws InvalidProductPriceException if the price, if the timestamp is in the future
     * @throws IllegalArgumentException     if storeProductId, price or timestamp is null
     */
    void addPrice(StoreProduct.StoreProductId storeProductId, Money price, LocalDateTime timestamp);

    /**
     * Get the prices for all competitors for a specific productId.
     *
     * @param productId     the ID of the product to get the prices.
     * @return              List of PriceDto.
     * @throws EntityNotFoundException  if a product for the productId does not exist.
     */
    List<PriceDto> getProductPricesByProductIt(Long productId) throws EntityNotFoundException;
}
