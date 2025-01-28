package ase.cogniprice.controller;

import ase.cogniprice.controller.dto.product.ProductCreateDto;
import ase.cogniprice.controller.dto.product.ProductDetailsDto;
import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.controller.dto.product.ProductUpdateDto;
import ase.cogniprice.controller.dto.store.product.StoreProductCreateDto;
import ase.cogniprice.controller.dto.store.product.StoreProductDetailsDto;
import ase.cogniprice.controller.dto.store.product.StoreProductWithPriceDto;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.service.ProductService;
import ase.cogniprice.service.StoreProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("/api/storeProducts")
public class StoreProductController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String BASE_PATH = "/api/storeProducts";
    private final StoreProductService storeProductService;

    @Autowired
    public StoreProductController(StoreProductService storeProductService) {
        this.storeProductService = storeProductService;
    }

    @Secured({"ROLE_USER"})
    @PostMapping("/create")
    @Operation(summary = "Create a new StoreProduct", description = "Allows a user to create a new store product and associate it with their account.")
    @ApiResponse(responseCode = "201", description = "Store product created successfully")
    @ApiResponse(responseCode = "422", description = "Invalid store product data")
    @ApiResponse(responseCode = "409", description = "Store product already exists")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> createStoreProduct(
        @Valid @RequestBody StoreProductCreateDto storeProductCreateDto,
        @AuthenticationPrincipal String username) {
        LOG.info("POST {}/create of {} with body: {}", BASE_PATH, username, storeProductCreateDto);

        storeProductService.createStoreProduct(storeProductCreateDto, username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_USER"})
    @DeleteMapping("/remove")
    @Operation(summary = "Remove a StoreProduct from the watchlist",
        description = "Allows a user to remove a store product from their watchlist.")
    @ApiResponse(responseCode = "204", description = "Store product removed successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Store product not found")
    public ResponseEntity<Void> removeStoreProduct(
        @RequestBody StoreProduct.StoreProductId storeProductId,
        @AuthenticationPrincipal String username) {

        LOG.info("DELETE {}/remove {} of {}", BASE_PATH, storeProductId, username);
        storeProductService.removeStoreProduct(storeProductId, username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Secured({"ROLE_USER"})
    @DeleteMapping("/removeAll")
    @Operation(summary = "Remove all StoreProducts of a product from the watchlist",
        description = "Allows a user to remove all store products associated with a specific product ID from their watchlist.")
    @ApiResponse(responseCode = "204", description = "All store products of product removed successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<Void> removeAllStoreProduct(
        @RequestBody Long productId,
        @AuthenticationPrincipal String username) {

        LOG.info("DELETE {}/remove of {}", BASE_PATH, productId);
        storeProductService.removeAllStoreProduct(productId, username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Secured({"ROLE_USER"})
    @PostMapping("/add")
    @Operation(summary = "Add a StoreProduct to the watchlist",
        description = "Allows a user to add a store product to their watchlist.")
    @ApiResponse(responseCode = "204", description = "Store product added successfully")
    @ApiResponse(responseCode = "404", description = "Store product not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> addStoreProduct(
        @RequestBody StoreProduct.StoreProductId storeProductId,
        @AuthenticationPrincipal String username) {

        LOG.info("POST {}/add {} of {}", BASE_PATH, storeProductId, username);
        storeProductService.addStoreProduct(storeProductId, username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Secured({"ROLE_USER"})
    @GetMapping("/view")
    @Operation(summary = "View StoreProducts with Prices and Images",
        description = "Retrieves a paginated list of store products" +
            " with price and image details for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Store products retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Page<ProductDetailsWithPricesDto>> getStoreProductsWithPriceAndImage(
        @AuthenticationPrincipal String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String name,
        @RequestParam(defaultValue = "") String category) {
        LOG.info("GET {}/view?page={}&size={}&name={}&category={} of {}", BASE_PATH, page, size, name, category, username);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDetailsWithPricesDto> products = storeProductService.getStoreProductsWithPriceAndImage(username, name, category, pageable);
        return ResponseEntity.ok(products);
    }
}
