package ase.cogniprice.unitTest.WebhookTest.serviceTest;

import ase.cogniprice.controller.dto.pricing.rule.PriceAdjustmentDto;
import ase.cogniprice.controller.dto.webhook.dto.WebhookRegistrationDto;
import ase.cogniprice.controller.mapper.WebhookMapper;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.ProductPrice;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.entity.Webhook;
import ase.cogniprice.exception.InvalidUrlException;
import ase.cogniprice.exception.UrlNotReachableException;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

//@SpringBootTest
public class WebhookCRUDServiceTest {

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
    void registerWebhook_success() {
        // Given
        WebhookRegistrationDto dto = new WebhookRegistrationDto();
        dto.setCallbackUrl("https://orf.at/");
        dto.setSecret("some-secret");

        Webhook mockWebhook = new Webhook();
        mockWebhook.setCallbackUrl(dto.getCallbackUrl());
        mockWebhook.setSecret(dto.getSecret());
        mockWebhook.setCreatedAt(LocalDateTime.now());

        Mockito.when(restTemplate.getForEntity(Mockito.eq(dto.getCallbackUrl()), Mockito.eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        // Configure the mock behavior for the mapper
        when(webhookMapper.toEntity(dto)).thenReturn(mockWebhook);

        // Configure the repository to return the test user
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);

        // When
        webhookService.registerWebhook(dto, testUser.getUsername());

        // Then
        verify(webhookMapper, times(1)).toEntity(dto); // Ensure the mapper was called
        verify(webhookRepository, times(1)).save(mockWebhook); // Ensure the repository was called with the mocked Webhook
        assertEquals(testUser, mockWebhook.getApplicationUser(), "Application user should be set correctly in the Webhook");
    }



    @Test
    void addProductToWebhook_success() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.of(testWebhook));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        // When
        webhookService.addProductToWebhook(testProduct.getId(), testUser.getUsername());

        // Then
        assertTrue(testWebhook.getProducts().contains(testProduct));
        assertTrue(testProduct.getWebhooks().contains(testWebhook));
        verify(webhookRepository, times(1)).save(testWebhook);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void addProductToWebhook_webhookNotFound() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.addProductToWebhook(testProduct.getId(), testUser.getUsername()));
        assertEquals("No webhook found for the user", exception.getMessage());
    }

    @Test
    void deleteWebhook_success() {
        // Given
        testWebhook.getProducts().add(testProduct);
        testProduct.getWebhooks().add(testWebhook);

        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.of(testWebhook));

        // When
        webhookService.deleteWebhook(testUser.getUsername());

        // Then
        assertFalse(testProduct.getWebhooks().contains(testWebhook));
        verify(productRepository, times(1)).save(testProduct);
        verify(webhookRepository, times(1)).delete(testWebhook);
    }

    @Test
    void deleteWebhook_webhookNotFound() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.deleteWebhook(testUser.getUsername()));
        assertEquals("No webhook found for the user", exception.getMessage());
    }

    @Test
    void removeProductFromWebhook_success() {
        // Given
        testWebhook.getProducts().add(testProduct);
        testProduct.getWebhooks().add(testWebhook);

        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.of(testWebhook));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        // When
        webhookService.removeProductFromWebhook(testProduct.getId(), testUser.getUsername());

        // Then
        assertFalse(testWebhook.getProducts().contains(testProduct));
        assertFalse(testProduct.getWebhooks().contains(testWebhook));
        verify(webhookRepository, times(1)).save(testWebhook);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void getWebhook_success() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.of(testWebhook));

        // When
        Webhook webhook = webhookService.getWebhook(testUser.getUsername());

        // Then
        assertEquals(testWebhook, webhook);
    }

    @Test
    void getWebhook_webhookNotFound() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.getWebhook(testUser.getUsername()));
        assertEquals("No webhook found for the user", exception.getMessage());
    }

    @Test
    void updateWebhookUrl_success() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.of(testWebhook));

        String newUrl = "https://new-webhook-url.com";

        Mockito.when(restTemplate.getForEntity(Mockito.eq(newUrl), Mockito.eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        // When
        webhookService.updateWebhookUrl(newUrl, testUser.getUsername());

        // Then
        assertEquals(newUrl, testWebhook.getCallbackUrl());
        verify(webhookRepository, times(1)).save(testWebhook);
    }

    @Test
    void updateWebhookUrl_webhookNotFound() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.empty());

        String newUrl = "https://orf.at/";
        Mockito.when(restTemplate.getForEntity(Mockito.eq(newUrl), Mockito.eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.updateWebhookUrl(newUrl, testUser.getUsername()));
        assertEquals("No webhook found for the user", exception.getMessage());
    }



    @Test
    void registerWebhook_invalidUrl_shouldThrowInvalidUrlException() {
        // Given
        WebhookRegistrationDto dto = new WebhookRegistrationDto();
        dto.setCallbackUrl("invalid-url");
        dto.setSecret("secret123");

        // When & Then
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> webhookService.registerWebhook(dto, testUser.getUsername()));
        assertEquals("Invalid callback URL: invalid-url", exception.getMessage());
    }

    @Test
    void registerWebhook_urlNotReachable_shouldThrowUrlNotReachableException() {
        // Given
        WebhookRegistrationDto dto = new WebhookRegistrationDto();
        dto.setCallbackUrl("https://unreachable-url.com");
        dto.setSecret("secret123");

        Mockito.when(restTemplate.getForEntity(dto.getCallbackUrl(), String.class))
                .thenThrow(new RuntimeException("Connection timed out"));

        // When & Then
        UrlNotReachableException exception = assertThrows(UrlNotReachableException.class,
                () -> webhookService.registerWebhook(dto, testUser.getUsername()));
        assertEquals("The provided URL is not reachable: https://unreachable-url.com", exception.getMessage());
    }

    @Test
    void deleteWebhook_webhookNotFound_shouldThrowNotFoundException() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> webhookService.deleteWebhook(testUser.getUsername()));
        assertEquals("No webhook found for the user", exception.getMessage());
    }

    @Test
    void updateWebhookUrl_invalidUrl_shouldThrowInvalidUrlException() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.of(testWebhook));

        String invalidUrl = "invalid-url";

        // When & Then
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> webhookService.updateWebhookUrl(invalidUrl, testUser.getUsername()));
        assertEquals("Invalid callback URL: invalid-url", exception.getMessage());
    }

    @Test
    void updateWebhookUrl_unreachableUrl_shouldThrowUrlNotReachableException() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.of(testWebhook));

        String unreachableUrl = "https://unreachable-url.com";
        when(restTemplate.getForEntity(unreachableUrl, String.class))
                .thenThrow(new RuntimeException("Connection timed out"));

        // When & Then
        UrlNotReachableException exception = assertThrows(UrlNotReachableException.class,
                () -> webhookService.updateWebhookUrl(unreachableUrl, testUser.getUsername()));
        assertEquals("The provided URL is not reachable: " + unreachableUrl, exception.getMessage());
    }

    @Test
    void removeProductFromWebhook_productNotFound_shouldThrowIllegalArgumentException() {
        // Given
        when(applicationUserRepository.findApplicationUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(webhookRepository.findByApplicationUser(testUser)).thenReturn(Optional.of(testWebhook));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> webhookService.removeProductFromWebhook(testProduct.getId(), testUser.getUsername()));
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void registerWebhook_nullOrEmptyUrl_shouldThrowInvalidUrlException() {
        // Given
        WebhookRegistrationDto dto = new WebhookRegistrationDto();
        dto.setCallbackUrl(""); // Empty URL
        dto.setSecret("secret123");

        // When & Then
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> webhookService.registerWebhook(dto, testUser.getUsername()));
        assertEquals("Invalid callback URL: ", exception.getMessage());
    }






}
