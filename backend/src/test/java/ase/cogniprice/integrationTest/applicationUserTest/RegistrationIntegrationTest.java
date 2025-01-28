package ase.cogniprice.integrationTest.applicationUserTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.type.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existinguser@example.com");
        String password = passwordEncoder.encode("password123!");
        existingUser.setPassword(password);
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setLoginAttempts(0L);
        existingUser.setUserLocked(false);
        existingUser.setRole(Role.USER);
        applicationUserRepository.save(existingUser);
    }

    @AfterEach
    public void tearDown() {
        applicationUserRepository.deleteAll(); // Cleanup after each test
    }

    @Test
    void testRegistrationSuccessful() throws Exception {
        String validRequestBody = """
                {
                    "username": "testuser",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "testuser@example.com",
                    "password": "Password123!"
                }
                """;

        // Expect successful registration
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/registration")
                        .contentType("application/json")
                        .content(validRequestBody))
                .andExpect(status().isCreated())
                .andReturn();

        // Verify the user is saved in the database
        ApplicationUser savedUser = applicationUserRepository.findApplicationUserByEmail("testuser@example.com");
        assertNotNull(savedUser, "User should be saved in the database");
        assertEquals("testuser", savedUser.getUsername(), "Username should match the input");
        assertEquals("John", savedUser.getFirstName(), "First Name should match the input");
    }

    @Test
    void testRegistrationUsernameAlreadyExists() throws Exception {
        String requestBody = """
                {
                    "username": "existinguser",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "newuser@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/registration")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("User with username existinguser already exists."));
    }

    @Test
    void testRegistrationEmailAlreadyExists() throws Exception {
        String requestBody = """
                {
                    "username": "newuser",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "existinguser@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/registration")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("User with email existinguser@example.com already exists."));
    } // User with email existinguser@example.com already exists

    @Test
    void testRegistrationValidationErrors() throws Exception {
        String invalidRequestBody = """
                {
                    "username": "",
                    "firstName": "John1",
                    "lastName": "Doe@",
                    "email": "invalidEmail",
                    "password": "pass"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/registration")
                        .contentType("application/json")
                        .content(invalidRequestBody))
                .andExpect(status().isUnprocessableEntity());
    }

}
