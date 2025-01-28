package ase.cogniprice.unitTest.WebhookTest;

import ase.cogniprice.controller.dto.webhook.dto.WebhookRegistrationDto;
import ase.cogniprice.controller.mapper.WebhookMapper;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class WebhookMapperTest {

    @Autowired
    private WebhookMapper webhookMapper;

    private ApplicationUser testUser;
    private Webhook testWebhook;
    private Product testProduct;


    @BeforeEach
    void setUp() {

        // Initialize test data
        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testWebhook = new Webhook();
        testWebhook.setId(1L);
        testWebhook.setCallbackUrl("https://example.com/webhook");
        testWebhook.setApplicationUser(testUser);
        testWebhook.setProducts(new HashSet<>());

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setWebhooks(new HashSet<>());
    }


    @Test
    void Webhook_Mapper_Test() {
        // Given
        WebhookRegistrationDto dto = new WebhookRegistrationDto("https://example.com/new-webhook", "optional-secret");

        // No need to mock the mapper as we are using the actual implementation
        Webhook webhook = webhookMapper.toEntity(dto);

        // Then
        assertEquals(dto.getCallbackUrl(), webhook.getCallbackUrl());
        assertEquals(dto.getSecret(), webhook.getSecret());
    }

}
