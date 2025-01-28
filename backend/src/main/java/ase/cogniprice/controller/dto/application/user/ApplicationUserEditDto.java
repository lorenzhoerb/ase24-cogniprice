package ase.cogniprice.controller.dto.application.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationUserEditDto {
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email has the wrong format")
    private String email;

    @AssertTrue(message = "Email exceeds 255 characters")
    private boolean isEmailTooLong() {
        return email.length() < 256;
    }

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,15}$", message = "Username must be 3-15 characters long and contain only letters, numbers, or underscores")
    private String username;

    @NotBlank(message = "First Name is required")
    @Pattern(regexp = "^[a-zA-Z ]*$", message = "First Name should contain only letters and spaces")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    @Pattern(regexp = "^[a-zA-Z ]*$", message = "Last Name should contain only letters and spaces")
    private String lastName;
}
