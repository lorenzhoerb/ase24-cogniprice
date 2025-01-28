package ase.cogniprice.controller.dto.competitor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompetitorUpdateDto {
    @NotNull(message = "Competitor name cannot be null")
    @Size(min = 1, max = 255, message = "Competitor name must be between 1 and 255 characters")
    private String name;
}
