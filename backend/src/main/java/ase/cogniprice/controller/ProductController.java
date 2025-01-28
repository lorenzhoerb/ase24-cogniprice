package ase.cogniprice.controller;

import ase.cogniprice.config.properties.SecurityProperties;
import ase.cogniprice.controller.dto.product.ProductBatchImporterDto;
import ase.cogniprice.controller.dto.product.ProductCreateDto;
import ase.cogniprice.controller.dto.product.ProductDetailsDto;
import ase.cogniprice.controller.dto.product.ProductDetailsWithCompetitorUrlDto;
import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.controller.dto.product.ProductUpdateDto;
import ase.cogniprice.controller.dto.store.product.StoreProductCreateDto;
import ase.cogniprice.controller.dto.store.product.StoreProductDetailsDto;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.service.ProductService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String BASE_PATH = "/api/products";

    private final ProductService productService;
    private final SecurityProperties securityProperties;

    @Autowired
    public ProductController(ProductService productService, SecurityProperties securityProperties) {
        this.productService = productService;
        this.securityProperties = securityProperties;
    }

    @Secured({"ROLE_USER"})
    @PostMapping("/create")
    @Operation(summary = "Create a new Product",
        description = "Allows a user to create a new product by providing product details and an optional image.")
    @ApiResponse(responseCode = "201", description = "Product created successfully,returning its ID")
    @ApiResponse(responseCode = "200", description = "Product already exists, returning its ID")
    @ApiResponse(responseCode = "422", description = "Invalid product data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<String> createProduct(
        @Valid @RequestPart("productCreateDto") ProductCreateDto productCreateDto,
        @RequestPart(value = "image", required = false)MultipartFile image) {
        LOG.info("POST {}/createProduct body: {}", BASE_PATH, productCreateDto);

        // Check if the product already exists (based on service logic)
        Product existingProduct = productService.getProductByGtin(productCreateDto.getGtin());
        if (existingProduct != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(existingProduct.getId().toString());
        }

        Product product = productService.createProduct(productCreateDto, image);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(product.getId().toString());
    }

    @Secured({"ROLE_USER"})
    @PostMapping("/batch-create")
    @Operation(summary = "Create a batch of new products",
            description = "Allows a user to create new products by providing product details as a JSON array")
    @ApiResponse(responseCode = "200", description = "Products created successfully, skipped already existing ones")
    @ApiResponse(responseCode = "422", description = "Invalid product data in the file")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<?> createProductBatchAndStoreProductsIfCompetitorProvided(
            @RequestBody List<@Valid ProductBatchImporterDto> batchProductsDto, @AuthenticationPrincipal String username) {
        LOG.info("POST {}/create-batch body: {}", BASE_PATH, batchProductsDto);
        productService.createBatchProducts(batchProductsDto, username);
        return ResponseEntity.status(HttpStatus.OK).body("All Products created successfully");
    }


    @Secured({"ROLE_USER"})
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing Product",
        description = "Allows a user to update product details and optionally upload a new image.")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "422", description = "Invalid product data")
    @ApiResponse(responseCode = "400", description = "ID mismatch")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<Product> updateProduct(
        @PathVariable Long id,
        @Valid @RequestPart("productUpdateDto") ProductUpdateDto productUpdateDto,
        @RequestPart(value = "image", required = false) MultipartFile image) {
        LOG.info("POST {}/{} body: {}", BASE_PATH, id, productUpdateDto);

        productUpdateDto.setImage(image);
        // Ensure the ID in the path matches the ID in the DTO
        if (!id.equals(productUpdateDto.getId())) {
            return ResponseEntity.badRequest().build(); // Return 400 if IDs don't match
        }

        // Call the service to update the product
        Product updatedProduct = productService.updateProduct(productUpdateDto);

        // Return the updated product with a 200 status
        return ResponseEntity.ok(updatedProduct);
    }

    @Secured({"ROLE_USER"})
    @GetMapping("/search")
    @Operation(summary = "Search for Products",
            description = "Allows a user to search for products by query, category and competitorId, with pagination.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Page<?>> searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(required = false) Long competitorId
    ) {
        LOG.info("GET {}/search?page={}&size={}&name={}&category={}&competitorId={}", BASE_PATH, page, size, query, category, competitorId);

        Pageable pageable = PageRequest.of(page, size);

        if (competitorId != null) {
            Page<ProductDetailsWithCompetitorUrlDto> products = this.productService.searchProductsByCompetitorId(query, category, pageable, competitorId);
            return ResponseEntity.ok(products);
        } else {
            Page<ProductDetailsDto> products = this.productService.searchProducts(query, category, pageable);
            return ResponseEntity.ok(products);
        }
    }

    @Secured({"ROLE_USER"})
    @GetMapping("/autocomplete")
    @Operation(summary = "Get Product Suggestions",
        description = "Provides autocomplete suggestions for products based on a query.")
    @ApiResponse(responseCode = "200", description = "Suggestions retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<List<ProductDetailsDto>> getSuggestions(
        @RequestParam("q") String query) {
        LOG.info("GET {}/autocomplete?q={}", BASE_PATH, query);
        List<ProductDetailsDto> products = this.productService.getSuggestions(query);
        return ResponseEntity.ok(products);
    }

    @Secured({"ROLE_USER"})
    @GetMapping("/details/{productId}")
    @Operation(summary = "Fetches a specific Product and the details by its Product ID")
    public ResponseEntity<ProductDetailsWithPricesDto> getProductDetailsById(
            @PathVariable Long productId) {
        LOG.info("GET {}/details/{}", BASE_PATH, productId);

        ProductDetailsWithPricesDto storeProduct = productService.getProductWithPriceAndImageById(productId);
        return ResponseEntity.ok(storeProduct);
    }

    // Get userId from the JWT claims
    private String getUsernameFromRequest(HttpServletRequest request) {
        LOG.trace("get username from request: {}", request);
        String authorizationHeader = request.getHeader("Authorization");
        String token = authorizationHeader.replace("Bearer ", "");
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(securityProperties.getJwtSecret().getBytes())
                .build();
        Claims claims = parser.parseClaimsJws(token).getBody();
        String username = claims.getSubject();
        return username;
    }
}
