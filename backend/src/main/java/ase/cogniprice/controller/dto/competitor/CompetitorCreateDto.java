package ase.cogniprice.controller.dto.competitor;

import ase.cogniprice.utils.HostnameUtils;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompetitorCreateDto {
    @NotNull(message = "Competitor name cannot be null")
    @Size(min = 1, max = 255, message = "Competitor name must be between 1 and 255 characters")
    private String name;
    @NotNull(message = "Competitor hostname cannot be null")
    @Pattern(regexp = HostnameUtils.REGEX_HOSTNAME_PATTERN, message = "Hostname must be valid and conform to RFC 1123")
    @Size(max = 255, message = "Competitor hostname must be 255 characters long at most")
    private String hostname;
}
