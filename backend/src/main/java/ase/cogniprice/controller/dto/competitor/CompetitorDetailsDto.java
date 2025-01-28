package ase.cogniprice.controller.dto.competitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompetitorDetailsDto {
    private Long id;
    private String name;
    private String hostname;
}
