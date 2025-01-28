package ase.cogniprice.controller.dto.crawler;

import java.math.BigDecimal;

public record MoneyDto(
        BigDecimal price,
        String currency
) {
}
