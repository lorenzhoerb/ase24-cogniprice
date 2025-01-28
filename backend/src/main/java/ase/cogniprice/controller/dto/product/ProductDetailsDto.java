package ase.cogniprice.controller.dto.product;

import lombok.Data;

@Data
public class ProductDetailsDto {
    private Long id;
    private String name;
    private String gtin;
    private String imageBase64;
    private String productImageUrl;
    private String category;
}
