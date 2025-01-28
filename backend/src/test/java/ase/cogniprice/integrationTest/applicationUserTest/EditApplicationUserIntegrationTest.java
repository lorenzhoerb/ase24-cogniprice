package ase.cogniprice.integrationTest.applicationUserTest;


import ase.cogniprice.config.JwtTestUtils;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.security.JwtTokenizer;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EditApplicationUserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenizer jwtTokenizer;

    private String validToken;

    ApplicationUser existingUser = new ApplicationUser();

    @BeforeEach
    public void setUp() {
        existingUser.setUsername("existingUser");
        existingUser.setEmail("existinguser@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123!"));
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setLoginAttempts(0L);
        existingUser.setUserLocked(false);
        existingUser.setRole(Role.USER);
        ApplicationUser saved =  applicationUserRepository.save(existingUser);

        validToken = jwtTokenizer.getAuthToken(existingUser.getUsername(), saved.getId(), List.of("ROLE_USER"));
    }

    @AfterEach
    public void tearDown() {
        applicationUserRepository.deleteAll(); // Clean up after each test
    }

    @Test
    void testEditApplicationUserSuccess1() throws Exception {
        String validRequestBody = """
                {
                    "username": "updatedUser",
                    "firstName": "Updated",
                    "lastName": "User",
                    "email": "existinguser@example.com"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/edit")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + validToken)
                        .content(validRequestBody))
                .andExpect(status().isOk());

        ApplicationUser updatedUser = applicationUserRepository.findApplicationUserByEmail("existinguser@example.com");
        assertNotNull(updatedUser, "User should be updated in the database");
        assertEquals("updatedUser", updatedUser.getUsername(), "Username should be updated");
        assertEquals("Updated", updatedUser.getFirstName(), "First name should be updated");
        assertEquals("User", updatedUser.getLastName(), "Last name should be updated");
    }

    @Test
    void testEditApplicationUserSuccess2() throws Exception {
        String validRequestBody = """
                {
                    "username": "existingUser",
                    "firstName": "Updated",
                    "lastName": "User",
                    "email": "updatedUser@example.com"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/edit")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + validToken)
                        .content(validRequestBody))
                .andExpect(status().isOk());

        ApplicationUser updatedUser = applicationUserRepository.findApplicationUserByUsername("existingUser");
        assertNotNull(updatedUser, "User should be updated in the database");
        assertEquals("updatedUser@example.com", updatedUser.getEmail(), "Email should be updated");
        assertEquals("Updated", updatedUser.getFirstName(), "First name should be updated");
        assertEquals("User", updatedUser.getLastName(), "Last name should be updated");
    }

    @Test
    void testEditApplicationUserUnauthorized() throws Exception {
        String validRequestBody = """
                {
                    "username": "updateduser",
                    "firstName": "Updated",
                    "lastName": "User",
                    "email": "updateduser@example.com"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/edit")
                        .contentType("application/json")
                        .header("Authorization", "Bearer invalid token")
                        .content(validRequestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testEditUserUsernameAlreadyExists() throws Exception {
        // Set up another user with a conflicting username
        ApplicationUser anotherUser = new ApplicationUser();
        anotherUser.setUsername("conflictinguser");
        anotherUser.setEmail("conflictinguser@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password456!"));
        anotherUser.setFirstName("Conflicting");
        anotherUser.setLastName("User");
        anotherUser.setLoginAttempts(0L);
        anotherUser.setUserLocked(false);
        anotherUser.setRole(Role.USER);
        applicationUserRepository.save(anotherUser);

        String requestBody = """
                {
                    "username": "conflictinguser",
                    "firstName": "Updated",
                    "lastName": "User",
                    "email": "updateduser@example.com"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/edit")
                        .contentType("application/json")
                        .header("Authorization", validToken)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("User with username conflictinguser already exists."));
    }

    @Test
    void testEditUserValidationError() throws Exception {
        String invalidRequestBody = """
                {
                    "username": "",
                    "firstName": "Updated1",
                    "lastName": "User@",
                    "email": "invalidEmail"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/edit")
                        .contentType("application/json")
                        .header("Authorization", validToken)
                        .content(invalidRequestBody))
                .andExpect(status().isUnprocessableEntity());
    }
}
