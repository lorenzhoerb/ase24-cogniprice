package ase.cogniprice.controller.dto.application.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class ApplicationUserRegistrationDto {
    @NotBlank(message = "Username is required")
    @NotNull(message = "Username must not be null")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,15}$", message = "Username must be 3-15 characters long and contain only letters, numbers, or underscores")
    private String username;

    @NotBlank(message = "First Name is required")
    @Pattern(regexp = "^[a-zA-Z ]*$", message = "First Name should contain only letters and spaces")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    @Pattern(regexp = "^[a-zA-Z ]*$", message = "Last Name should contain only letters and spaces")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @AssertTrue(message = "Email exceeds 255 characters")
    private boolean isEmailTooLong() {
        return email.length() < 256;
    }

    @NotBlank(message = "Password is required")
    @Size(min = 7, message = "Password must be at least 7 characters long")
    @Pattern(regexp = "^(?=.*[!@#$%^&+=])[a-zA-Z0-9!@#$%^&+=]*$",
            message = "Password must contain at least one special character (!@#$%^&+=) and no invalid characters")
    private String password;

}
