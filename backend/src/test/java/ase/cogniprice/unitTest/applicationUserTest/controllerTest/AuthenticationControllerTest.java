package ase.cogniprice.unitTest.applicationUserTest.controllerTest;

import ase.cogniprice.controller.dto.application.user.ApplicationUserRegistrationDto;
import ase.cogniprice.controller.dto.authentication.AuthRequest;
import ase.cogniprice.exception.InvalidPasswordException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.UserLockedException;
import ase.cogniprice.exception.UsernameAlreadyExistsException;
import ase.cogniprice.service.ApplicationUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationUserService applicationUserService;

    private AuthRequest validAuthRequest;
    private AuthRequest invalidAuthRequest;
    private ApplicationUserRegistrationDto validRegistrationRequest;

    @BeforeEach
    public void setUp() {
        // Setup authentication requests
        validAuthRequest = new AuthRequest("testuser", "password123");
        invalidAuthRequest = new AuthRequest("testuser", "wrongpassword");

        // Setup registration request
        validRegistrationRequest = new ApplicationUserRegistrationDto();
        validRegistrationRequest.setUsername("testuser");
        validRegistrationRequest.setPassword("password123!");
        validRegistrationRequest.setEmail("testuser@gmail.com");
        validRegistrationRequest.setFirstName("testuser");
        validRegistrationRequest.setLastName("testuser");
    }

    @Test
    void testLoginSuccessful() throws Exception {
        // Mock the login service
        when(applicationUserService.loginApplicationUser(validAuthRequest)).thenReturn("token123");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{ \"username\": \"testuser\", \"password\": \"password123\" }"))
            .andExpect(status().isOk())
            .andExpect(content().string("token123"))
            .andReturn();

        // Optionally assert the result directly
        assertEquals("token123", result.getResponse().getContentAsString());
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        // Mock the exception for invalid password
        when(applicationUserService.loginApplicationUser(invalidAuthRequest)).thenThrow(new InvalidPasswordException("Invalid password"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{ \"username\": \"testuser\", \"password\": \"wrongpassword\" }"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("Invalid password"));
    }

    @Test
    void testLoginUserNotFound() throws Exception {
        // Mock the exception for user not found
        when(applicationUserService.loginApplicationUser(invalidAuthRequest)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{ \"username\": \"testuser\", \"password\": \"wrongpassword\" }"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("User not found"));
    }

    @Test
    void testLoginUserLocked() throws Exception {
        // Mock the exception for user locked
        when(applicationUserService.loginApplicationUser(validAuthRequest)).thenThrow(new UserLockedException("Account locked"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{ \"username\": \"testuser\", \"password\": \"password123\" }"))
            .andExpect(status().isLocked())
            .andExpect(content().string("Account locked"));
    }


    // Registration tests
    @Test
    void testRegisterSuccessful() throws Exception {
        when(applicationUserService.registerUser(validRegistrationRequest)).thenReturn("Registration successful");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/registration")
                        .contentType("application/json")
                        .content("{ \"username\": \"testuser\", \"firstName\": \"testuser\", \"lastName\": \"testuser\", \"email\": \"testuser@gmail.com\", \"password\": \"password123!\" }"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Registration successful"));
    }

    @Test
    void testRegistrationDuplicateUsername() throws Exception {
        when(applicationUserService.registerUser(validRegistrationRequest)).thenThrow(new UsernameAlreadyExistsException("Username already exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/registration")
                        .contentType("application/json")
                        .content("{ \"username\": \"testuser\", \"firstName\": \"testuser\", \"lastName\": \"testuser\", \"email\": \"testuser@gmail.com\", \"password\": \"password123!\" }"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Username already exists"));
    }

}
