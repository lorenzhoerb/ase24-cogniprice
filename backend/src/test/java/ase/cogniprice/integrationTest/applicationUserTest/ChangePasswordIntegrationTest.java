package ase.cogniprice.integrationTest.applicationUserTest;

import ase.cogniprice.config.JwtTestUtils;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChangePasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTestUtils jwtTestUtils;

    private ApplicationUser user;
    private String token = "test";
    private PasswordResetToken resetToken;

    private final String newPassword = "newPassword!";

    private final String oldPassword = "password!";

    private String jwt;

    @BeforeEach
    void setUp() {
        user = new ApplicationUser();
        user.setUsername("regularUser");

        user.setPassword(oldPassword);
        user.setFirstName("User");
        user.setLastName("Test");
        user.setEmail("user@email.com");
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        user.setRole(Role.USER);
        user.setStoreProducts(new HashSet<>());
        resetToken = new PasswordResetToken(user, token);
        user.setPasswordResetToken(resetToken);
        applicationUserRepository.save(user);

        jwt = jwtTestUtils.generateToken("regularUser", List.of("ROLE_USER"));
    }

    @Test
    void testChangePassword_WithExistingUser_Returns204() throws Exception {

        String requestBody = """
        {
            "token": "%s",
            "newPassword": "%s"
        }
        """.formatted(token, newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changePassword")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        assertTrue(passwordEncoder.matches("newPassword!",this.applicationUserRepository
            .findApplicationUserByUsername(this.user.getUsername()).getPassword()));
    }

    @Test
    void testChangePassword_WithWrongToken_Returns404() throws Exception {

        String requestBody = """
        {
            "token": "%s",
            "newPassword": "%s"
        }
        """.formatted("invalidToken", newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changePassword")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        assertEquals("password!", this.applicationUserRepository
            .findApplicationUserByUsername(this.user.getUsername()).getPassword());
    }

    @Test
    void testChangePassword_WithExpiredToken_Returns401() throws Exception {

        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        Date expiryDate = Date.from(twoDaysAgo.toInstant(ZoneOffset.UTC));
        resetToken.setExpiryDate(expiryDate);
        this.passwordTokenRepository.save(resetToken);

        String requestBody = """
        {
            "token": "%s",
            "newPassword": "%s"
        }
        """.formatted(token, newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changePassword")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        assertEquals("password!", this.applicationUserRepository
            .findApplicationUserByUsername(this.user.getUsername()).getPassword());
    }

    @Test
    void testChangePasswordLoggedIn_WithExistingUser_Returns204() throws Exception {

        user.setPassword(passwordEncoder.encode(oldPassword));
        applicationUserRepository.save(user);

        String requestBody = """
        {
            "oldPassword": "%s",
            "newPassword": "%s"
        }
        """.formatted(oldPassword, newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/changePassword")
                .header("Authorization", "Bearer " + jwt)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        assertTrue(passwordEncoder.matches(newPassword,this.applicationUserRepository
            .findApplicationUserByUsername(this.user.getUsername()).getPassword()));
    }

    @Test
    void testChangePasswordLoggedIn_WithExistingUserAndWrongPassword_Returns401() throws Exception {

        user.setPassword(passwordEncoder.encode(oldPassword));
        applicationUserRepository.save(user);

        String requestBody = """
        {
            "oldPassword": "%s",
            "newPassword": "%s"
        }
        """.formatted(passwordEncoder.encode("wrongPass"), newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/changePassword")
                .header("Authorization", "Bearer " + jwt)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
        }

    @Test
    void testChangePasswordLoggedIn_NonExistingUser_Returns404() throws Exception {

        String requestBody = """
        {
            "oldPassword": "%s",
            "newPassword": "%s"
        }
        """.formatted(passwordEncoder.encode(oldPassword), newPassword);

        jwt = jwtTestUtils.generateToken("nonExisting", List.of("ROLE_USER"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/changePassword")
                .header("Authorization", "Bearer " + jwt)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

}
