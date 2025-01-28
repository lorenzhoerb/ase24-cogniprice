package ase.cogniprice.integrationTest.ApiKeyIntegrationTest;


import ase.cogniprice.entity.ApiKey;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.repository.ApiKeyRepository;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.security.ApiKeyEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiKeyIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ApiKeyEncoder apiKeyEncoder;

    private ApplicationUser user;
    private String rawApiKey;

    @BeforeEach
    void setUp() {
        apiKeyRepository.deleteAll();
        applicationUserRepository.deleteAll();

        user = setApplicationUser();
        user = applicationUserRepository.save(user);

        ApiKey apiKey = setApiKey();
        apiKeyRepository.save(apiKey);
    }

    @AfterEach
    void tearDown() {
        apiKeyRepository.deleteAll();
        applicationUserRepository.deleteAll();
    }

    @Test
    void testEndpointWithValidApiKey() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/details")
                        .header("Authorization", "apiKey " + rawApiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().intValue()))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void testEndpointWithInvalidApiKey() throws Exception {
        String invalidApiKey = "invalid_api_key";
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/details")
                        .header("Authorization", "apiKey " + invalidApiKey))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertEquals("Invalid authorization header or token",
                        result.getResponse().getContentAsString(),
                        "The response body should match the expected error message"));
    }

    @Test
    void testExpiredApiKey() throws Exception {
        // Update the API key to be expired
        ApiKey expiredApiKey = apiKeyRepository.findByAccessKey(apiKeyEncoder.encodeApiKey(rawApiKey)).orElseThrow();
        expiredApiKey.setExpiresAt(LocalDateTime.now().minusDays(1));
        apiKeyRepository.save(expiredApiKey);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/details")
                        .header("Authorization", "apiKey " + rawApiKey))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertEquals("Invalid authorization header or token",
                        result.getResponse().getContentAsString(),
                        "The response body should match the expected error message"));
    }

    private ApplicationUser setApplicationUser() {
        user = new ApplicationUser();
        user.setUsername("test_user");
        user.setEmail("test_user@example.com");
        user.setPassword("password!");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(ase.cogniprice.type.Role.USER);
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        return user;
    }

    private ApiKey setApiKey() {
        rawApiKey = "raw_api_key_value";
        String encodedApiKey = apiKeyEncoder.encodeApiKey(rawApiKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setAccessKey(encodedApiKey);
        apiKey.setMaskedKey("apiKey a7e4b********************a63c");
        apiKey.setApplicationUser(user);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(LocalDateTime.now().plusDays(90));
        return apiKey;
    }
}
