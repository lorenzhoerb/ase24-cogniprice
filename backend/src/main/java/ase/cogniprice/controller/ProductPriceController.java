package ase.cogniprice.controller;

import ase.cogniprice.controller.dto.store.product.PriceDto;
import ase.cogniprice.service.ProductPriceService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("/api/productPrices")
public class ProductPriceController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String BASE_PATH = "/api/productPrices";
    private final ProductPriceService productPriceService;

    @Autowired
    public ProductPriceController(ProductPriceService productPriceService) {
        this.productPriceService = productPriceService;
    }

    @Secured({"ROLE_USER"})
    @GetMapping("/{productId}")
    @Operation(summary = "Get all prices for a product, grouped by competitor")
    public ResponseEntity<List<PriceDto>> getPriceTrends(
            @PathVariable Long productId) {
        LOG.info("GET {}/{}", BASE_PATH, productId);

        List<PriceDto> priceTrends = productPriceService.getProductPricesByProductIt(productId);
        return ResponseEntity.ok(priceTrends);
    }
}
