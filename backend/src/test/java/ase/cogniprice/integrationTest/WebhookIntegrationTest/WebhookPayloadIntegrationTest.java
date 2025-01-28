package ase.cogniprice.integrationTest.WebhookIntegrationTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.Webhook;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.WebhookRepository;
import ase.cogniprice.security.JwtTokenizer;
import ase.cogniprice.type.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ExtendWith(MockitoExtension.class)
public class WebhookPayloadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private WebhookRepository webhookRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @MockBean
    private RestTemplate restTemplate;



    @Autowired
    private JwtTokenizer jwtTokenizer;

    private ApplicationUser user;
    private String jwt;

    @BeforeEach
    public void setUp() {
        user = new ApplicationUser();
        user.setUsername("testuser");
        user.setPassword("encodedPassword"); // Mock encoded password
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        user.setRole(Role.USER);
        user = applicationUserRepository.save(user);

        jwt = jwtTokenizer.getAuthToken(user.getUsername(), user.getId(), List.of("ROLE_USER"));
    }

    @AfterEach
    public void tearDown() {
        // Fetch all webhooks
        List<Webhook> webhooks = webhookRepository.findAll();
        for (Webhook webhook : webhooks) {
            // Clear the relationship on both sides
            for (Product product : webhook.getProducts()) {
                product.getWebhooks().remove(webhook);
            }
            webhook.getProducts().clear();
            webhookRepository.save(webhook);
        }

        // Proceed with deletion
        webhookRepository.deleteAll();
        productRepository.deleteAll();
        productCategoryRepository.deleteAll();
        applicationUserRepository.deleteAll();
    }

    @Test
    void sendWebhookData_success() throws Exception {
        // Create and save a ProductCategory
        ProductCategory category = new ProductCategory();
        category.setCategory("Electronics");
        productCategoryRepository.save(category);

        // Create and save a Product
        Product product = new Product();
        product.setName("Test Product");
        product.setGtin("1234567890123");
        product.setProductCategory(category);
        productRepository.save(product);

        // Create and save a Webhook linked to the Product
        Webhook webhook = new Webhook();
        webhook.setCallbackUrl("https://example.com/webhook"); // Ensure valid URL
        webhook.setSecret("secret123");
        webhook.setApplicationUser(user);
        webhook.getProducts().add(product);
        webhook.setCreatedAt(LocalDateTime.now());
        webhookRepository.save(webhook);

        // Mock RestTemplate response for webhook callback
        Mockito.when(restTemplate.postForEntity(
                Mockito.eq("https://example.com/webhook"),
                Mockito.any(),
                Mockito.eq(String.class)
        )).thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Perform the POST request to trigger the webhook data sending
        mockMvc.perform(post("/api/webhook/send-data")
                        .header("Authorization", jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook data sent successfully"));

        // Verify that the webhook data was sent
        Mockito.verify(restTemplate).postForEntity(
                Mockito.eq("https://example.com/webhook"),
                Mockito.argThat(payload -> {
                    // Verify the payload structure
                    Map<String, Object> payloadMap = (Map<String, Object>) payload;
                    assertEquals("trigger_webhook", payloadMap.get("event"));
                    List<Map<String, Object>> products = (List<Map<String, Object>>) payloadMap.get("registered_products");
                    assertEquals(1, products.size());
                    Map<String, Object> productData = products.get(0);
                    assertEquals("1234567890123", productData.get("GTIN"));
                    return true;
                }),
                Mockito.eq(String.class)
        );
    }
}
