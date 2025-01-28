package ase.cogniprice.controller;

import ase.cogniprice.controller.dto.product.category.ProductCategoryDto;
import ase.cogniprice.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
public class ProductCategoryController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProductCategoryService productCategoryService;

    @Autowired
    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @Operation(summary = "Get all product categories", description = "Retrieve a list of all product categories.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of product categories retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search input")
    })
    @GetMapping
    public List<ProductCategoryDto> getAllProductCategories(@RequestParam(required = false) String name) {
        LOG.info("getAllProductCategories({})", name);
        return name == null || name.isBlank()
                ? productCategoryService.getAllProductCategories()
                : productCategoryService.getProductCategoriesByName(name);
    }
}
