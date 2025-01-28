package ase.cogniprice.controller.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductUpdateDto {

    @NotNull
    private Long id;

    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters")
    private String name;

    @Pattern(regexp = "^[0-9]{8,13}$", message = "GTIN must be between 8 and 13 digits")
    private String gtin;

    private String productCategoryId;

    private MultipartFile image;

}
