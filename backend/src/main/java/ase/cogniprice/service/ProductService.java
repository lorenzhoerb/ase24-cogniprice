package ase.cogniprice.service;

import ase.cogniprice.controller.dto.product.ProductBatchImporterDto;
import ase.cogniprice.controller.dto.product.ProductCreateDto;
import ase.cogniprice.controller.dto.product.ProductDetailsDto;
import ase.cogniprice.controller.dto.product.ProductDetailsWithCompetitorUrlDto;
import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.controller.dto.product.ProductUpdateDto;
import ase.cogniprice.controller.dto.store.product.StoreProductDetailsDto;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.GtinLookupException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    /**
     * Creates a new product using the provided product creation details.
     * If the product already exists in the database, it returns the existing product.
     * Otherwise, it performs a GTIN lookup to validate the product details before saving it.
     *
     * @param productCreateDto the details of the product to create.
     * @param image the image of the product
     * @return the newly created product or the existing product if it already exists.
     * @throws GtinLookupException if the GTIN lookup fails or the GTIN is invalid.
     */
    Product createProduct(ProductCreateDto productCreateDto, MultipartFile image);


    /**
     * Creates a batch of new products using the list of provided products details.
     * If a product already exists in the database, it aborts and sends back a message about which product is causing problems.
     *
     * @param productBatchDtos List with details of products to create.
     * @throws GtinLookupException if the GTIN lookup fails or the GTIN is invalid.
     */
    void createBatchProducts(List<ProductBatchImporterDto> productBatchDtos, String username);

    /**
     * Updates an existing product with the provided update details.
     *
     * @param productUpdateDto the details to update the product with.
     * @return the updated product entity.
     * @throws EntityNotFoundException if the product with the specified ID does not exist.
     * @throws IllegalArgumentException if the update details are invalid or inconsistent.
     */
    Product updateProduct(ProductUpdateDto productUpdateDto);

    /**
     * Retrieves a list of products associated with a given user.
     * This method queries the database for products that are associated with the specified
     * username. It maps the retrieved `Product` entities to `ProductReturnDto` objects
     * using the `productMapper` and returns the result as a list of `ProductReturnDto` objects.
     *
     * @param username the username of the user for whom the products are to be retrieved
     * @return a list of {@link ProductDetailsDto} representing the products associated with the user
     */
    List<ProductDetailsDto> getProductsOfUser(String username);

    /**
     * Retrieves a list of product suggestions based on a search query.
     * The suggestions include products matching the query across all categories.
     *
     * @param query the search query used to find matching products
     * @return a list of {@link ProductDetailsDto} containing product suggestions
     */
    List<ProductDetailsDto> getSuggestions(String query);

    /**
     * Retrieves a product by its GTIN (Global Trade Item Number).
     *
     * @param gtin the GTIN of the product
     * @return the {@link Product} entity associated with the given GTIN
     * @throws EntityNotFoundException if no product is found for the specified GTIN
     */
    Product getProductByGtin(String gtin);

    /**
     * Searches for products based on a search query, category, and pagination details.
     * The results are filtered by the provided query and category, if specified.
     *
     * @param query    the search query to filter products
     * @param category the category to filter products (empty for all categories)
     * @param pageable the pagination and sorting details
     * @return a {@link Page} of {@link ProductDetailsDto} containing the matching products
     */
    Page<ProductDetailsDto> searchProducts(String query, String category, Pageable pageable);

    /**
     * Searches for products based on a search query, category, pagination, and competitorId details.
     * The results are filtered by the provided query and category, if specified.
     *
     * @param query    the search query to filter products
     * @param category the category to filter products (empty for all categories)
     * @param pageable the pagination and sorting details
     * @param competitorId the competitor from whom we want to retrieve the products
     * @return a {@link Page} of {@link ProductDetailsDto} containing the matching products
     */
    Page<ProductDetailsWithCompetitorUrlDto> searchProductsByCompetitorId(String query, String category, Pageable pageable, Long competitorId);

    /**
     * Retrieves detailed information about a product, including price and image, by its ID.
     *
     * @param productId the ID of the product
     * @return a {@link ProductDetailsWithPricesDto} containing detailed product information
     * @throws EntityNotFoundException if no product is found for the specified ID
     */
    ProductDetailsWithPricesDto getProductWithPriceAndImageById(Long productId);
}
