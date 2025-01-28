package ase.cogniprice.integrationTest.WebhookIntegrationTest;

import ase.cogniprice.controller.dto.webhook.dto.WebhookRegistrationDto;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WebhookIntegrationTest {

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

    @Autowired
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
    void registerWebhook_success() throws Exception {
        String payload = """
            {
                "callbackUrl": "https://orf.at/",
                "secret": "secret123"
            }
            """;

        mockMvc.perform(post("/api/webhook/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwt)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().string("Sucessfully created Webhook!"));

        Webhook savedWebhook = webhookRepository.findByApplicationUser(user).orElse(null);
        assertNotNull(savedWebhook);
        assertEquals("https://orf.at/", savedWebhook.getCallbackUrl());
    }

    @Test
    void getWebhook_success() throws Exception {
        Webhook webhook = new Webhook();
        webhook.setCallbackUrl("https://example.com/webhook");
        webhook.setSecret("secret123");
        webhook.setApplicationUser(user);
        webhook.setCreatedAt(LocalDateTime.now());
        webhookRepository.save(webhook);

        mockMvc.perform(get("/api/webhook")
                        .header("Authorization", jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callbackUrl").value("https://example.com/webhook"))
                .andExpect(jsonPath("$.secret").value("secret123"));
    }

    @Test
    void deleteWebhook_success() throws Exception {
        Webhook webhook = new Webhook();
        webhook.setCallbackUrl("https://example.com/webhook");
        webhook.setSecret("secret123");
        webhook.setApplicationUser(user);
        webhook.setCreatedAt(LocalDateTime.now());
        webhookRepository.save(webhook);

        mockMvc.perform(delete("/api/webhook")
                        .header("Authorization", jwt))
                .andExpect(status().isNoContent());

        assertFalse(webhookRepository.findByApplicationUser(user).isPresent());
    }

    @Test
    void updateWebhookUrl_success() throws Exception {
        Webhook webhook = new Webhook();
        webhook.setCallbackUrl("https://example.com/old-webhook");
        webhook.setSecret("secret123");
        webhook.setApplicationUser(user);
        webhook.setCreatedAt(LocalDateTime.now());
        webhookRepository.save(webhook);

        String newUrl = "https://orf.at/";
        WebhookRegistrationDto dto = new WebhookRegistrationDto(newUrl, "");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/webhook/update-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwt)
                        .content(jsonRequestBody)) // Send as JSON object
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook URL updated successfully"));

        Webhook updatedWebhook = webhookRepository.findByApplicationUser(user).orElse(null);
        assertNotNull(updatedWebhook);
        assertEquals("https://orf.at/", updatedWebhook.getCallbackUrl());
    }


    @Test
    void addProductToWebhook_success() throws Exception {
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

        // Create and save a Webhook
        Webhook webhook = new Webhook();
        webhook.setCallbackUrl("https://example.com/webhook");
        webhook.setSecret("secret123");
        webhook.setApplicationUser(user);
        webhook.setCreatedAt(LocalDateTime.now());
        webhookRepository.save(webhook);

        // Perform the request
        mockMvc.perform(post("/api/webhook/add-product/" + product.getId())
                        .header("Authorization", jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("Product added to webhook successfully"));

        // Verify the product was added to the webhook
        Webhook updatedWebhook = webhookRepository.findById(webhook.getId()).orElse(null);
        assertNotNull(updatedWebhook);
        assertTrue(updatedWebhook.getProducts().contains(product));
    }

    @Test
    void removeProductFromWebhook_success() throws Exception {
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

        // Create and save a Webhook with the product
        Webhook webhook = new Webhook();
        webhook.setCallbackUrl("https://example.com/webhook");
        webhook.setSecret("secret123");
        webhook.setApplicationUser(user);
        webhook.getProducts().add(product);
        webhook.setCreatedAt(LocalDateTime.now());
        webhookRepository.save(webhook);

        // Perform the request
        mockMvc.perform(delete("/api/webhook/remove-product/" + product.getId())
                        .header("Authorization", jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("Product removed from webhook successfully"));

        // Verify the product was removed from the webhook
        Webhook updatedWebhook = webhookRepository.findById(webhook.getId()).orElse(null);
        assertNotNull(updatedWebhook);
        assertFalse(updatedWebhook.getProducts().contains(product));
    }

    @Test
    void deleteWebhookWithProducts_success() throws Exception {
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
        webhook.setCallbackUrl("https://example.com/webhook");
        webhook.setSecret("secret123");
        webhook.setApplicationUser(user);
        webhook.getProducts().add(product);
        webhook.setCreatedAt(LocalDateTime.now());
        webhookRepository.save(webhook);

        // Verify initial state
        assertTrue(webhookRepository.findById(webhook.getId()).isPresent());
        assertTrue(productRepository.findById(product.getId()).isPresent());

        // Perform the delete request for the webhook
        mockMvc.perform(delete("/api/webhook")
                        .header("Authorization", jwt))
                .andExpect(status().isNoContent());

        // Verify the webhook is deleted
        assertFalse(webhookRepository.findById(webhook.getId()).isPresent());

        // Verify the product no longer contains the webhook
        Product updatedProduct = productRepository.findById(product.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertFalse(updatedProduct.getWebhooks().contains(webhook));
    }



}

