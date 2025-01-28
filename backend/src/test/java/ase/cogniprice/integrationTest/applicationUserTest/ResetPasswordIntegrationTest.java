package ase.cogniprice.integrationTest.applicationUserTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.PasswordResetToken;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.PasswordTokenRepository;
import ase.cogniprice.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResetPasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;
    private ApplicationUser user;
    @BeforeEach
    void setUp() {
        user = new ApplicationUser();
        user.setUsername("regularUser");
        user.setPassword("password!");
        user.setFirstName("User");
        user.setLastName("Test");
        user.setEmail("user@email.com");
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        user.setRole(Role.USER);
        user.setStoreProducts(new HashSet<>());
        applicationUserRepository.save(user);
    }

    @Test
    void testResetPassword_WithExistingUser_Returns204() throws Exception {

        String requestBody = """
                {
                    "email" : "user@email.com"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/resetPassword")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isNoContent());
        assertNotNull(this.passwordTokenRepository.findByUser(this.user));
    }

    @Test
    void testResetPassword_WithExistingUserAndAlreadyExistingToken_Returns204() throws Exception {

        this.passwordTokenRepository.save(new PasswordResetToken(this.user, "token"));
        String requestBody = """
                {
                    "email" : "user@email.com"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/resetPassword")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isNoContent());
        assertNotNull(this.passwordTokenRepository.findByUser(this.user));
    }

    @Test
    void testResetPassword_WithNonExistingUser_Returns404() throws Exception {
        this.passwordTokenRepository.save(new PasswordResetToken(this.user, "token"));

        String requestBody = """
                {
                    "email" : "notExistingUser@email.com"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/resetPassword")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isNotFound());
    }
}
