package ase.cogniprice.unitTest.WebhookTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Webhook;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.WebhookRepository;
import ase.cogniprice.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebhookRepositoryTest {

    @Autowired
    private WebhookRepository webhookRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    private ApplicationUser user;
    private Webhook webhook;

    @BeforeEach
    public void setUp() {
        // Clean up database
        webhookRepository.deleteAll();
        applicationUserRepository.deleteAll();

        // Create a test user
        user = new ApplicationUser();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        user.setRole(Role.USER);
        applicationUserRepository.save(user);

        // Create a test webhook
        webhook = new Webhook();
        webhook.setCallbackUrl("https://example.com/webhook");
        webhook.setSecret("optional-secret");
        webhook.setCreatedAt(LocalDateTime.now());
        webhook.setApplicationUser(user);
    }

    @Test
    @Rollback
    void testCreateWebhook() {
        Webhook savedWebhook = webhookRepository.save(webhook);
        assertNotNull(savedWebhook.getId(), "Webhook ID should not be null after saving");
        assertEquals("https://example.com/webhook", savedWebhook.getCallbackUrl(), "Callback URL should match");
        assertEquals("optional-secret", savedWebhook.getSecret(), "Secret should match");
        assertNotNull(savedWebhook.getCreatedAt(), "CreatedAt should not be null");
        assertEquals(user.getId(), savedWebhook.getApplicationUser().getId(), "Associated user should match");
    }

    @Test
    @Rollback
    void testReadWebhook() {
        Webhook savedWebhook = webhookRepository.save(webhook);
        Optional<Webhook> foundWebhook = webhookRepository.findById(savedWebhook.getId());
        assertTrue(foundWebhook.isPresent(), "Webhook should be found by ID");
        assertEquals("https://example.com/webhook", foundWebhook.get().getCallbackUrl(), "Callback URL should match");
        assertEquals("optional-secret", foundWebhook.get().getSecret(), "Secret should match");
    }

    @Test
    @Rollback
    void testUpdateWebhook() {
        Webhook savedWebhook = webhookRepository.save(webhook);
        savedWebhook.setCallbackUrl("https://newexample.com/webhook");
        Webhook updatedWebhook = webhookRepository.save(savedWebhook);
        assertEquals("https://newexample.com/webhook", updatedWebhook.getCallbackUrl(), "Callback URL should be updated");
    }

    @Test
    @Rollback
    void testDeleteWebhook() {
        Webhook savedWebhook = webhookRepository.save(webhook);
        webhookRepository.delete(savedWebhook);
        Optional<Webhook> deletedWebhook = webhookRepository.findById(savedWebhook.getId());
        assertFalse(deletedWebhook.isPresent(), "Webhook should be deleted and not found");
    }
}
