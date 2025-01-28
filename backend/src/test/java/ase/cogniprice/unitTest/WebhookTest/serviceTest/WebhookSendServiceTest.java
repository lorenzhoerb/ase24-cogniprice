package ase.cogniprice.unitTest.WebhookTest.serviceTest;

import ase.cogniprice.controller.dto.pricing.rule.PriceAdjustmentDto;
import ase.cogniprice.controller.mapper.WebhookMapper;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.ProductPrice;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.entity.Webhook;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.PricingRuleRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.WebhookRepository;
import ase.cogniprice.service.implementation.WebhookImpl;
import ase.cogniprice.type.Role;
import jakarta.persistence.EntityNotFoundException;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebhookSendServiceTest {


    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WebhookRepository webhookRepository;

    @Mock
    private WebhookMapper webhookMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    @InjectMocks
    private WebhookImpl webhookService;

    private ApplicationUser testUser;
    private Webhook testWebhook;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testWebhook = new Webhook();
        testWebhook.setId(1L);
        testWebhook.setCallbackUrl("https://orf.at/");
        testWebhook.setApplicationUser(testUser);
        testWebhook.setProducts(new HashSet<>());

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setWebhooks(new HashSet<>());
    }
    @Test
    void testTriggerBatchWebhookForUser_withAllEntities() {
        // Create a ProductCategory
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory("Electronics");

        // Create a Competitor
        Competitor competitor = new Competitor();
        competitor.setId(1L);
        competitor.setName("Competitor A");
        competitor.setUrl("http://competitor-a.com");

        // Create an ApplicationUser
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setId(1L);
        applicationUser.setUsername("testUser");
        applicationUser.setPassword("testPassword");
        applicationUser.setFirstName("Test");
        applicationUser.setLastName("User");
        applicationUser.setEmail("test.user@example.com");
        applicationUser.setLoginAttempts(0L);
        applicationUser.setUserLocked(false);
        applicationUser.setRole(Role.USER);

        // Create a Product
        Product product = new Product();
        product.setId(1L);
        product.setName("Smartphone");
        product.setGtin("1234567890123");
        product.setProductCategory(productCategory);
        productCategory.getProducts().add(product); // Establish bi-directional relationship

        StoreProduct.StoreProductId storeProductId= new StoreProduct.StoreProductId(1L, 1L);

        // Create ProductPrice
        ProductPrice productPrice = new ProductPrice();
        ProductPrice.ProductPriceId priceId = new ProductPrice.ProductPriceId(
                storeProductId,
                LocalDateTime.now().minusDays(1) // Example timestamp
        );
        productPrice.setId(priceId);
        productPrice.setPrice(Money.of(100, "USD")); // Set price and currency using Money

        // Create a Webhook
        Webhook webhook = new Webhook();
        webhook.setId(1L);
        webhook.setCallbackUrl("http://webhook-url.com");
        webhook.setApplicationUser(applicationUser); // Link ApplicationUser to the Webhook
        webhook.setProducts(Set.of(product)); // Associate product with the webhook
        applicationUser.setNotifications(Set.of()); // Optional: clear or add notifications

        // Mock Repositories
        when(applicationUserRepository.findApplicationUserByUsername("testUser"))
                .thenReturn(applicationUser);
        when(webhookRepository.findByApplicationUser(applicationUser))
                .thenReturn(Optional.of(webhook));

        // Mock RestTemplate
        doReturn(new ResponseEntity<>(HttpStatus.OK)).when(restTemplate).postForEntity(
                eq(webhook.getCallbackUrl()),
                any(),
                eq(String.class)
        );

        // Trigger the batch webhook
        webhookService.triggerBatchWebhookForUser("testUser");

        // Verify RestTemplate interaction
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(restTemplate).postForEntity(
                eq(webhook.getCallbackUrl()),
                payloadCaptor.capture(),
                eq(String.class)
        );

        // Verify payload structure
        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertEquals("trigger_webhook", capturedPayload.get("event"));
        assertNotNull(capturedPayload.get("registered_products"));
        assertTrue(capturedPayload.get("registered_products") instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = (List<Map<String, Object>>) capturedPayload.get("registered_products");
        assertEquals(1, products.size());

        Map<String, Object> productData = products.get(0);
        assertEquals("1234567890123", productData.get("GTIN"));

        // Validate timestamp
        assertNotNull(capturedPayload.get("timestamp"));
    }

    @Test
    void triggerBatchWebhookForUser_webhookNotFound_shouldThrowEntityNotFoundException() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.triggerBatchWebhookForUser(testUser.getUsername()));
        assertEquals("No webhook found for the user", exception.getMessage());
    }

    @Test
    void triggerBatchWebhookForUser_noProducts_shouldThrowEntityNotFoundException() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.of(testWebhook));

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.triggerBatchWebhookForUser(testUser.getUsername()));
        assertEquals("No products associated with the webhook", exception.getMessage());
    }

    @Test
    void triggerWebhookForPriceAdjustmentDto_success() {
        // Given
        PriceAdjustmentDto priceAdjustmentDto = new PriceAdjustmentDto();
        priceAdjustmentDto.setUserId(1L);
        priceAdjustmentDto.setProductId(1L);
        priceAdjustmentDto.setPricingRuleId(1L);
        priceAdjustmentDto.setAdjustedPrice(BigDecimal.valueOf(100.00));
        priceAdjustmentDto.setAveragePrice(BigDecimal.valueOf(95.00));
        priceAdjustmentDto.setCheapestPrice(BigDecimal.valueOf(90.00));
        priceAdjustmentDto.setHighestPrice(BigDecimal.valueOf(110.00));
        priceAdjustmentDto.setAdjustmentDate(ZonedDateTime.now());

        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setId(1L);
        applicationUser.setUsername("testUser");

        Product product = new Product();
        product.setId(1L);
        product.setGtin("1234567890123");

        PricingRule pricingRule = new PricingRule();
        pricingRule.setId(1L);
        pricingRule.setName("Test Pricing Rule");

        Webhook webhook = new Webhook();
        webhook.setId(1L);
        webhook.setCallbackUrl("https://example.com/webhook");
        webhook.setApplicationUser(applicationUser);

        when(applicationUserRepository.findById(priceAdjustmentDto.getUserId())).thenReturn(Optional.of(applicationUser));
        when(webhookRepository.findByApplicationUser(applicationUser)).thenReturn(Optional.of(webhook));
        when(productRepository.findById(priceAdjustmentDto.getProductId())).thenReturn(Optional.of(product));
        when(pricingRuleRepository.findById(priceAdjustmentDto.getPricingRuleId())).thenReturn(Optional.of(pricingRule));
        when(restTemplate.postForEntity(eq(webhook.getCallbackUrl()), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        webhookService.triggerWebhookForPriceAdjustmentDto(priceAdjustmentDto);

        // Then
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(restTemplate).postForEntity(eq(webhook.getCallbackUrl()), payloadCaptor.capture(), eq(String.class));

        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertEquals("priceChange", capturedPayload.get("event"));
        Map<String, Object> productData = (Map<String, Object>) capturedPayload.get("product");
        assertEquals("1234567890123", productData.get("GTIN"));
        assertEquals(BigDecimal.valueOf(100.00), productData.get("adjustedPrice"));
        assertEquals(BigDecimal.valueOf(95.00), productData.get("averagePrice"));
        assertEquals(BigDecimal.valueOf(90.00), productData.get("cheapestPrice"));
        assertEquals(BigDecimal.valueOf(110.00), productData.get("highestPrice"));
        assertNotNull(productData.get("adjustmentDate"));
        assertNotNull(capturedPayload.get("timestamp"));
    }

    @Test
    void triggerWebhookForPriceAdjustmentDto_productNotFound() {
        // Given
        PriceAdjustmentDto priceAdjustmentDto = new PriceAdjustmentDto();
        priceAdjustmentDto.setUserId(1L);
        priceAdjustmentDto.setProductId(999L); // Non-existing product ID
        priceAdjustmentDto.setPricingRuleId(1L);

        when(applicationUserRepository.findById(priceAdjustmentDto.getUserId()))
                .thenReturn(Optional.of(new ApplicationUser()));
        when(webhookRepository.findByApplicationUser(any(ApplicationUser.class)))
                .thenReturn(Optional.of(new Webhook()));
        when(productRepository.findById(priceAdjustmentDto.getProductId()))
                .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.triggerWebhookForPriceAdjustmentDto(priceAdjustmentDto));
        assertEquals("No product found with ID: 999", exception.getMessage());
    }

    @Test
    void triggerWebhookForPriceAdjustmentDto_webhookNotFound() {
        // Given
        PriceAdjustmentDto priceAdjustmentDto = new PriceAdjustmentDto();
        priceAdjustmentDto.setUserId(1L);
        priceAdjustmentDto.setProductId(1L);
        priceAdjustmentDto.setPricingRuleId(1L);

        when(applicationUserRepository.findById(priceAdjustmentDto.getUserId()))
                .thenReturn(Optional.of(new ApplicationUser()));
        when(webhookRepository.findByApplicationUser(any(ApplicationUser.class)))
                .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.triggerWebhookForPriceAdjustmentDto(priceAdjustmentDto));
        assertEquals("No webhook found for the user", exception.getMessage());
    }

    @Test
    void triggerWebhookForPriceAdjustmentDto_pricingRuleNotFound() {
        // Given
        PriceAdjustmentDto priceAdjustmentDto = new PriceAdjustmentDto();
        priceAdjustmentDto.setUserId(1L);
        priceAdjustmentDto.setProductId(1L);
        priceAdjustmentDto.setPricingRuleId(999L); // Non-existing pricing rule ID

        when(applicationUserRepository.findById(priceAdjustmentDto.getUserId()))
                .thenReturn(Optional.of(new ApplicationUser()));
        when(webhookRepository.findByApplicationUser(any(ApplicationUser.class)))
                .thenReturn(Optional.of(new Webhook()));
        when(productRepository.findById(priceAdjustmentDto.getProductId()))
                .thenReturn(Optional.of(new Product()));
        when(pricingRuleRepository.findById(priceAdjustmentDto.getPricingRuleId()))
                .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.triggerWebhookForPriceAdjustmentDto(priceAdjustmentDto));
        assertEquals("No pricing rule found with ID: 999", exception.getMessage());
    }


}
