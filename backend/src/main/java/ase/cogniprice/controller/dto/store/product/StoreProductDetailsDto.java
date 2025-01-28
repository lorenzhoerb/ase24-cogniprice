package ase.cogniprice.controller.dto.store.product;

import ase.cogniprice.entity.StoreProduct;
import lombok.Data;

import java.util.Base64;

@Data
public class StoreProductDetailsDto {
    private StoreProduct.StoreProductId storeProductId;
    private String productName;
    private String gtin;
    private String category;
    private String productImageBase64;
    private PriceDto lowestPrice;
    private PriceDto highestPrice;

    public StoreProductDetailsDto(StoreProduct.StoreProductId storeProductId, String productName, String gtin,
                                    byte[] productImage, String category, PriceDto lowestPrice, PriceDto highestPrice) {
        this.storeProductId = storeProductId;
        this.productName = productName;
        this.gtin = gtin;
        this.productImageBase64 = (productImage != null) ? Base64.getEncoder().encodeToString(productImage) : null;
        this.category = category;
        this.lowestPrice = lowestPrice;
        this.highestPrice = highestPrice;
    }
}
