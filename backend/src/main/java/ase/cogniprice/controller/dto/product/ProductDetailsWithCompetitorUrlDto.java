package ase.cogniprice.controller.dto.product;

import lombok.Data;

import java.util.Base64;

@Data
public class ProductDetailsWithCompetitorUrlDto {
    private Long id;
    private String name;
    private String gtin;
    private String imageBase64;
    private String productImageUrl;
    private String category;
    private String productUrl;

    public ProductDetailsWithCompetitorUrlDto(Long productId, String productName, String gtin,
                                       byte[] productImage, String productImageUrl, String category, String productUrl) {
        this.id = productId;
        this.name = productName;
        this.gtin = gtin;
        this.imageBase64 = (productImage != null) ? Base64.getEncoder().encodeToString(productImage) : null;
        this.productImageUrl = productImageUrl;
        this.category = category;
        this.productUrl = productUrl;
    }
}
