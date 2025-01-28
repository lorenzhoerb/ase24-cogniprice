package ase.cogniprice.controller.dto.product;

import ase.cogniprice.controller.dto.store.product.PriceDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

@Data
@NoArgsConstructor
public class ProductDetailsWithPricesDto {
    private Long productId;
    private String productName;
    private String gtin;
    private String category;
    private String productImageBase64;
    private String productImageUrl;
    private PriceDto lowestPrice;
    private PriceDto highestPrice;

    public ProductDetailsWithPricesDto(Long productId, String productName, String gtin,
                                  byte[] productImage, String productImageUrl, String category, PriceDto lowestPrice, PriceDto highestPrice) {
        this.productId = productId;
        this.productName = productName;
        this.gtin = gtin;
        this.productImageBase64 = (productImage != null) ? Base64.getEncoder().encodeToString(productImage) : null;
        this.productImageUrl = productImageUrl;
        this.category = category;
        this.lowestPrice = lowestPrice;
        this.highestPrice = highestPrice;
    }

    public ProductDetailsWithPricesDto(Long productId, String productName, String gtin,
                                    byte[] thumbnail, String productImageUrl, String category) {
        this.productId = productId;
        this.productName = productName;
        this.gtin = gtin;
        this.productImageBase64 = (thumbnail != null) ? Base64.getEncoder().encodeToString(thumbnail) : null;
        this.productImageUrl = productImageUrl;
        this.category = category;
    }

    @Override
    public String toString() {
        return "ProductDetailsWithPricesDto{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", gtin='" + gtin + '\'' +
                ", category='" + category + '\'' +
                ", lowestPrice=" + lowestPrice +
                ", highestPrice=" + highestPrice +
                '}';
    }
}
