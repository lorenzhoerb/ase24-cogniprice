package ase.cogniprice.controller;

import ase.cogniprice.controller.dto.application.user.ApplicationUserRegistrationDto;
import ase.cogniprice.controller.dto.authentication.AuthRequest;
import ase.cogniprice.controller.dto.password.PasswordDto;
import ase.cogniprice.service.ApplicationUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private static final String BASE_PATH = "/api/auth";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ApplicationUserService applicationUserService;

    @Autowired
    public AuthenticationController(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    @PermitAll
    @PostMapping("/registration")
    @Operation(summary = "Register a new User", description = "Registers a new user and returns a JWT token.")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "422", description = "Invalid registration data")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    public ResponseEntity<String> registerApplicationUser(
        @Valid @RequestBody ApplicationUserRegistrationDto
            applicationUserRegistrationDto) {
        LOG.info("POST {}/registration body: {}", BASE_PATH, applicationUserRegistrationDto);
        String jwt = applicationUserService.registerUser(applicationUserRegistrationDto);
        return new ResponseEntity<>(jwt, HttpStatus.CREATED);
    }

    @PermitAll
    @PostMapping("/login")
    @Operation(summary = "Authenticate a user", description = "Authenticates a user and returns a JWT token.")
    @ApiResponse(responseCode = "200", description = "User authenticated successfully")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "423", description = "User account is locked")
    public ResponseEntity<String> loginApplicationUser(
        @Valid @RequestBody AuthRequest authRequest) {
        LOG.info("POST {}/login body: {}", BASE_PATH, authRequest);

        String jwt = applicationUserService.loginApplicationUser(authRequest);
        return new ResponseEntity<>(jwt, HttpStatus.OK);
    }

    @PermitAll
    @PostMapping("/resetPassword")
    @Operation(summary = "Reset a user's password", description = "Sends a password reset link to the user's email.")
    @ApiResponse(responseCode = "204", description = "Password reset link sent successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<String>  resetPassword(@RequestBody Map<String, String> req) {
        String userEmail = req.get("email");
        LOG.info("POST {}/resetPassword body: {}", BASE_PATH, userEmail);

        applicationUserService.resetPassword(userEmail);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PermitAll
    @PostMapping("/changePassword")
    @Operation(summary = "Change a user's password", description = "Changes the user's password to a new one.")
    @ApiResponse(responseCode = "204", description = "Password changed successfully")
    @ApiResponse(responseCode = "422", description = "Invalid password data")
    @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    public ResponseEntity<String>  changePassword(@RequestBody @Valid PasswordDto passwordDto) {
        LOG.info("POST {}/changePassword body: {}", BASE_PATH, passwordDto);

        applicationUserService.changePassword(passwordDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
