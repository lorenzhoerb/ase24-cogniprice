package ase.cogniprice.entity;

import ase.cogniprice.controller.dto.pricing.rule.PriceLimitDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "price_limit")
public class PriceLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type")
    private LimitType limitType;

    @Column(name = "limit_value")
    private BigDecimal limitValue;

    public enum LimitType {
        FIXED_AMOUNT
    }

    public static PriceLimit from(PriceLimitDto priceLimitDto) {
        if (priceLimitDto == null) {
            return null;
        }
        PriceLimit priceLimit = new PriceLimit();
        priceLimit.setLimitType(priceLimitDto.getLimitType());
        priceLimit.setLimitValue(priceLimitDto.getLimitValue());
        return priceLimit;
    }
}



