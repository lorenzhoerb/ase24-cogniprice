package ase.cogniprice.service;

import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.controller.dto.store.product.StoreProductCreateDto;
import ase.cogniprice.controller.dto.store.product.StoreProductWithPriceDto;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.DomainValidationException;
import ase.cogniprice.exception.StoreProductAlreadyExistsException;
import ase.cogniprice.exception.UrlNotReachableException;
import ase.cogniprice.repository.StoreProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreProductService {

    /**
     * Creates a new store product and associates it with the specified user.
     *
     * @param storeProductCreateDto the details of the store product to create.
     * @param username              the username of the user to associate the store product with.
     * @throws EntityNotFoundException            if the product or competitor does not exist.
     * @throws StoreProductAlreadyExistsException if the store product already exists.
     * @throws DomainValidationException          if the product URL does not contain the competitor's domain.
     * @throws UrlNotReachableException           if the product URL is not reachable or returns not 2xx.
     */
    void createStoreProduct(StoreProductCreateDto storeProductCreateDto, String username)
        throws EntityNotFoundException, StoreProductAlreadyExistsException;

    /**
     * Removes a store product from the user's watchlist.
     *
     * @param storeProductId the ID of the store product to remove.
     * @param username       the username of the user whose watchlist will be updated.
     * @throws EntityNotFoundException if the store product or user does not exist.
     */
    void removeStoreProduct(StoreProduct.StoreProductId storeProductId, String username)
        throws EntityNotFoundException;

    /**
     * Adds an existing store product to the user's watchlist.
     *
     * @param storeProductId the ID of the store product to add.
     * @param username       the username of the user whose watchlist will be updated.
     * @throws EntityNotFoundException            if the store product or user does not exist.
     * @throws StoreProductAlreadyExistsException if the store product is already in the user's watchlist.
     */
    void addStoreProduct(StoreProduct.StoreProductId storeProductId, String username) throws EntityNotFoundException;

    /**
     * Retrieves a paginated list of store products associated with the specified user.
     * Each store product includes details such as the product's name, category, thumbnail image,
     * and information about the lowest and highest prices recorded for the product.
     *
     * @param username the username of the application user whose store products are to be retrieved
     * @param name     a filter for the product name (can be empty or null to fetch all products)
     * @param category a filter for the product category (can be empty or null to fetch all categories)
     * @param pageable the pagination and sorting information
     * @return a paginated {@link Page} of {@link ProductDetailsWithPricesDto}
     *         containing the store product details, including prices and images
     * @throws EntityNotFoundException if no user is found with the specified username
     */
    Page<ProductDetailsWithPricesDto> getStoreProductsWithPriceAndImage(String username, String name, String category, Pageable pageable);

    /**
     * Removes all instances of a specific store product from the specified user's store product watchlist.
     * This action ensures that the product is completely disassociated from the user.
     *
     * @param productId the ID of the product to be removed
     * @param username  the username of the application user from whose watchlist the product will be removed
     * @throws EntityNotFoundException if the specified user or product is not found
     */
    void removeAllStoreProduct(Long productId, String username) throws EntityNotFoundException;
}
