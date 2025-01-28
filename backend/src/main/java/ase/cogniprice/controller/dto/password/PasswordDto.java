package ase.cogniprice.controller.dto.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordDto {

    @NotBlank(message = "Password is required")
    @Size(min = 7, message = "Password must be at least 7 characters long")
    @Pattern(regexp = "^(?=.*[!@#$%^&+=])[a-zA-Z0-9!@#$%^&+=]*$",
        message = "Password must contain at least one special character (!@#$%^&+=) and no invalid characters")
    private String newPassword;
    private String oldPassword;
    private String token;
}
