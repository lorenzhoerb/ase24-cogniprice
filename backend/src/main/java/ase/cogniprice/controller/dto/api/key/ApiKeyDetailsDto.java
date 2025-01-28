package ase.cogniprice.controller.dto.api.key;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiKeyDetailsDto {

    @NotBlank
    private LocalDateTime createdAt;

    @NotBlank
    private LocalDateTime expiresAt;

    @NotBlank
    private String maskedKey;
}