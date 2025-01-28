package ase.cogniprice.unitTest.securityTest;

import ase.cogniprice.controller.dto.api.key.ApiKeyDetailsDto;
import ase.cogniprice.entity.ApiKey;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.repository.ApiKeyRepository;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.security.ApiKeyEncoder;
import ase.cogniprice.service.ApplicationUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ApiKeySecurityTest {

    @Autowired
    private ApplicationUserService applicationUserService;

    @Autowired
    private ApiKeyEncoder apiKeyEncoder;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    private ApplicationUser user;

    @BeforeEach
    void setUp() {
        apiKeyRepository.deleteAll();
        applicationUserRepository.deleteAll();
        user = this.setApplicationUser();
        applicationUserRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        apiKeyRepository.deleteAll();
        applicationUserRepository.deleteAll();
    }

    @Test
    void testGenerateApiKey() {
        // Given
        String rawApiKey = applicationUserService.generateApiKey(user.getUsername());

        // Then
        assertNotNull(rawApiKey, "API Key should not be null");
        String encodedKey = apiKeyEncoder.encodeApiKey(rawApiKey);

        // Proves that the keys are equal
        ApiKey savedKey = apiKeyRepository.findByAccessKey(encodedKey).get();

        assertNotNull(savedKey, "Saved API Key should not be null");
        assertEquals(user.getId(), savedKey.getApplicationUser().getId(), "API Key should belong to the correct user");
        assertNotNull(savedKey.getExpiresAt(), "API Key should have an expiration date");
    }

    @Test
    void testExpiredApiKey() {
        // Given
        ApiKey expiredApiKey = new ApiKey();
        String encodedApiKeyString = apiKeyEncoder.encodeApiKey("expired_key");
        expiredApiKey.setAccessKey(encodedApiKeyString);
        expiredApiKey.setApplicationUser(user);
        expiredApiKey.setMaskedKey("apiKey a7e4b********************a63c");
        expiredApiKey.setCreatedAt(LocalDateTime.now().minusDays(91));
        expiredApiKey.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired yesterday
        apiKeyRepository.save(expiredApiKey);

        // When
        ApiKey apiKey = apiKeyRepository.findByAccessKey(encodedApiKeyString).get();

        // Then
        assertTrue(apiKey.isExpired(), "Api Key should be expired");
    }

    @Test
    void testGetApiKeyDetails() {
        // Given
        String rawApiKey = applicationUserService.generateApiKey(user.getUsername());

        // When
        ApiKeyDetailsDto apiKeyDetails = applicationUserService.getApiKeyDetails(user.getUsername());

        // Then
        assertNotNull(apiKeyDetails, "API Key details should not be null");
        assertEquals(rawApiKey.substring(0, 3), apiKeyDetails.getMaskedKey().substring(0, 3), "API Key should belong to the correct user");
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
}
