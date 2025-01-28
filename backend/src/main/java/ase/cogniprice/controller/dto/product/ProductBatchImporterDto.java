package ase.cogniprice.controller.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ProductBatchImporterDto {
    @NotNull(message = "Product name cannot be null")
    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters")
    private String name;

    @Pattern(regexp = "^[0-9]{8,13}$", message = "GTIN must be between 8 and 13 digits")
    private String gtin;

    @NotNull(message = "Product category cannot be null")
    private String productCategory;

    @URL
    private String productImageUrl;

    // StoreProduct Attributes
    @Size(min = 1, max = 255, message = "Competitor name must be between 1 and 255 characters")
    private String competitor;

    @URL
    private String productUrl;
}
