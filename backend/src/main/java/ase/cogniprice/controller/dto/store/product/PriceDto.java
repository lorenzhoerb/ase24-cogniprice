package ase.cogniprice.controller.dto.store.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceDto {
    private Long competitorId;
    private BigDecimal price;
    private String competitorName;
    private String currency;
    private LocalDateTime priceTime;

    public PriceDto(Long competitorId, BigDecimal price, String competitorName, String currency, LocalDateTime priceTime) {
        this.competitorId = competitorId;
        this.price = price;
        this.competitorName = competitorName;
        this.currency = currency;
        this.priceTime = priceTime;
    }
}
