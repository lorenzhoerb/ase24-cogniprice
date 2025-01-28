package ase.cogniprice.integrationTest.applicationUserTest;

import ase.cogniprice.controller.dto.authentication.AuthRequest;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.security.JwtTokenizer;
import ase.cogniprice.service.ApplicationUserService;
import ase.cogniprice.exception.InvalidPasswordException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.UserLockedException;
import ase.cogniprice.type.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ApplicationUserService applicationUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    private ApplicationUser user;
    private String jwt;

    @BeforeEach
    public void setUp() {
        user = new ApplicationUser();
        user.setUsername("testuser");
        String password = passwordEncoder.encode("password123");
        user.setPassword(password);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        user.setRole(Role.USER);
        user = applicationUserRepository.save(user);
        UserDetails userDetails = applicationUserService.loadUserByUsername(user.getUsername());
        List<String> roles = userDetails.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        jwt = jwtTokenizer.getAuthToken(user.getEmail(), user.getId(), roles);
    }

    @AfterEach
    public void tearDown() {
        applicationUserRepository.delete(user);
    }

    @Test
    void testLoginSuccessful() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{ \"username\": \"testuser\", \"password\": \"password123\" }"))
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(result.getResponse().getContentAsString().startsWith("Bearer "));
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        // Simulate invalid password response from the service
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{ \"username\": \"testuser\", \"password\": \"wrongpassword\" }"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("Password or Username incorrect"));
        ApplicationUser foundUser = applicationUserRepository.findById(user.getId()).orElse(null);
        assertNotNull(foundUser);
        assertEquals(1, foundUser.getLoginAttempts(), "login attempts should be incremented");
    }

    @Test
    void testLoginUserNotFound() throws Exception {
        // Simulate user not found response from the service
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{ \"username\": \"nonexistentuser\", \"password\": \"password123\" }"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("User with username nonexistentuser not found."));
    }

    @Test
    void testLoginUserLocked() throws Exception {
        user.setUserLocked(true);
        applicationUserRepository.save(user);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{ \"username\": \"testuser\", \"password\": \"password123\" }"))
            .andExpect(status().isLocked())
            .andExpect(content().string("Account locked"));
    }

    @Test
    void testLoginUserGetsLocked() throws Exception {
        user.setLoginAttempts(4L);
        applicationUserRepository.save(user);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{ \"username\": \"testuser\", \"password\": \"wrongpassword\" }"))
            .andExpect(status().isLocked())
            .andExpect(content().string("Account locked due to too many failed attempts."));
    }
}
