package ase.cogniprice.integrationTest.applicationUserTest;


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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DeleteApplicationUserIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    private String validToken;

    private ApplicationUser existingUser;

    @BeforeEach
    public void setUp() {
        existingUser = new ApplicationUser();
        existingUser.setUsername("testuser");
        existingUser.setEmail("testuser@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123!"));
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
        existingUser.setLoginAttempts(0L);
        existingUser.setUserLocked(false);
        existingUser.setRole(Role.USER);
        ApplicationUser savedUser = applicationUserRepository.save(existingUser);

        validToken = jwtTokenizer.getAuthToken(existingUser.getUsername(), savedUser.getId(), List.of("ROLE_USER"));
    }

    @AfterEach
    public void tearDown() {
        applicationUserRepository.deleteAll(); // Clean up after each test
    }

    @Test
    void testDeleteApplicationUserSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        assertFalse(applicationUserRepository.findById(existingUser.getId()).isPresent(), "User should be deleted from the database");
    }

    @Test
    void testDeleteApplicationUserUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteApplicationUserNotFound() throws Exception {
        applicationUserRepository.deleteAll(); // Ensure no users exist

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

}
