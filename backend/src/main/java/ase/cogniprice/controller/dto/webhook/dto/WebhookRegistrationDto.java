package ase.cogniprice.controller.dto.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebhookRegistrationDto {

    @NotBlank(message = "Callback URL is required")
    private String callbackUrl;

    private String secret; // Optional: null if not provided
}
