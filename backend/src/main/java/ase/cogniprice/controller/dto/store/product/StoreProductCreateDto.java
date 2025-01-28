package ase.cogniprice.controller.dto.store.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class StoreProductCreateDto {

    @NotNull
    private Long productId;

    @NotNull
    private Long competitorId;

    @NotNull
    @URL
    private String productUrl;
}
