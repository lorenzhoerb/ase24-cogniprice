package ase.cogniprice.controller.dto.store.product;

import ase.cogniprice.entity.StoreProduct;
import lombok.Data;

import java.util.Base64;

@Data
public class StoreProductWithPriceDto {

    private Long productId;
    private String productName;
    private String category;
    private String thumbnailBase64;
    private PriceDto lowestPrice;
    private PriceDto highestPrice;

    public StoreProductWithPriceDto(Long productId, String productName,
                                    byte[] thumbnail, String category, PriceDto lowestPrice, PriceDto highestPrice) {
        this.productId = productId;
        this.productName = productName;
        this.thumbnailBase64 = (thumbnail != null) ? Base64.getEncoder().encodeToString(thumbnail) : null;
        this.category = category;
        this.lowestPrice = lowestPrice;
        this.highestPrice = highestPrice;
    }

    public StoreProductWithPriceDto(Long productId, String productName,
                                    byte[] thumbnail, String category) {
        this.productId = productId;
        this.productName = productName;
        this.thumbnailBase64 = (thumbnail != null) ? Base64.getEncoder().encodeToString(thumbnail) : null;
        this.category = category;
    }


}