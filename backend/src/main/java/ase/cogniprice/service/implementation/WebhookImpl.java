package ase.cogniprice.service.implementation;

import ase.cogniprice.controller.dto.pricing.rule.PriceAdjustmentDto;
import ase.cogniprice.controller.dto.webhook.dto.WebhookRegistrationDto;
import ase.cogniprice.controller.mapper.ApplicationUserMapper;
import ase.cogniprice.controller.mapper.WebhookMapper;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.Webhook;
import ase.cogniprice.exception.ConflictException;
import ase.cogniprice.exception.InvalidUrlException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.UrlNotReachableException;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.PricingRuleRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.WebhookRepository;
import ase.cogniprice.service.WebhookService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WebhookImpl implements WebhookService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ApplicationUserRepository applicationUserRepository;
    private final ProductRepository productRepository;
    private final WebhookRepository webhookRepository;
    private final WebhookMapper webhookMapper;
    private final RestTemplate restTemplate;
    private final PricingRuleRepository pricingRuleRepository;

    @Autowired
    public WebhookImpl(ApplicationUserRepository applicationUserRepository, ProductRepository productRepository,
                       WebhookRepository webhookRepository, WebhookMapper webhookMapper, RestTemplate restTemplate,
                       PricingRuleRepository pricingRuleRepository) {
        this.applicationUserRepository = applicationUserRepository;
        this.productRepository = productRepository;
        this.webhookRepository = webhookRepository;
        this.webhookMapper = webhookMapper;
        this.restTemplate = restTemplate;
        this.pricingRuleRepository = pricingRuleRepository;
    }

    @Override
    public void registerWebhook(WebhookRegistrationDto webhookRegistrationDto, String username) {

        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(username);

        if (webhookRepository.findByApplicationUser(applicationUser).isPresent()) {
            throw new ConflictException("Already registered webhook");
        }

        if (!isValidUrl(webhookRegistrationDto.getCallbackUrl())) {
            throw new InvalidUrlException("Invalid callback URL: " + webhookRegistrationDto.getCallbackUrl());
        }

        // Validate URL reachability
        if (!isUrlReachable(webhookRegistrationDto.getCallbackUrl())) {
            throw new UrlNotReachableException("The provided URL is not reachable: " + webhookRegistrationDto.getCallbackUrl());
        }

        // Map DTO to Webhook entity
        Webhook webhook = webhookMapper.toEntity(webhookRegistrationDto);

        // Set associated user

        webhook.setApplicationUser(applicationUser);

        // Save webhook
        webhookRepository.save(webhook);

        LOG.info("Webhook registered successfully for user {}", username);
    }

    private boolean isUrlReachable(String url) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException ex) {
            // Log client errors
            LOG.error("HTTP error when validating URL {}: {}", url, ex.getMessage());
            return false;
        } catch (Exception ex) {
            // Log other errors
            LOG.error("Error when validating URL {}: {}", url, ex.getMessage());
            return false;
        }
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try {
            URL parsedUrl = new URL(url);
            return "https".equalsIgnoreCase(parsedUrl.getProtocol());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void addProductToWebhook(Long productId, String username) {
        Webhook webhook = webhookRepository.findByApplicationUser(applicationUserRepository.findApplicationUserByUsername(username))
                .orElseThrow(() -> new EntityNotFoundException("No webhook found for the user"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Add product to webhook
        webhook.getProducts().add(product);
        product.getWebhooks().add(webhook);

        // Save both entities
        webhookRepository.save(webhook);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteWebhook(String username) {
        // Find the webhook for the user
        Webhook webhook = webhookRepository.findByApplicationUser(applicationUserRepository.findApplicationUserByUsername(username))
                .orElseThrow(() -> new EntityNotFoundException("No webhook found for the user"));

        // Remove references to the webhook in all related products
        Set<Product> products = webhook.getProducts();

        if (products != null) {
            for (Product product : products) {
                product.getWebhooks().remove(webhook);
                productRepository.save(product);
            }
        }


        // Delete the webhook
        webhookRepository.delete(webhook);

        LOG.info("Webhook for user {} has been successfully deleted.", username);
    }

    @Override
    @Transactional
    public void removeProductFromWebhook(Long productId, String username) {
        // Find the webhook for the user
        Webhook webhook = webhookRepository.findByApplicationUser(applicationUserRepository.findApplicationUserByUsername(username))
                .orElseThrow(() -> new EntityNotFoundException("No webhook found for the user"));

        // Find the product to remove
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Manually remove the product-webhook association
        webhook.getProducts().remove(product);
        product.getWebhooks().remove(webhook);

        // Save both entities
        webhookRepository.save(webhook);
        productRepository.save(product);

        LOG.info("Product ID {} has been removed from the webhook for user {}.", productId, username);
    }

    @Override
    @Transactional
    public Webhook getWebhook(String username) {
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(username);
        return webhookRepository.findByApplicationUser(applicationUser)
                .orElseThrow(() -> new EntityNotFoundException("No webhook found for the user"));
    }

    @Override
    @Transactional
    public void updateWebhookUrl(String newUrl, String username) {

        if (!isValidUrl(newUrl)) {
            throw new InvalidUrlException("Invalid callback URL: " + newUrl);
        }

        // Validate URL reachability
        if (!isUrlReachable(newUrl)) {
            throw new UrlNotReachableException("The provided URL is not reachable: " + newUrl);
        }

        // Find the webhook associated with the user
        Webhook webhook = webhookRepository.findByApplicationUser(applicationUserRepository.findApplicationUserByUsername(username))
                .orElseThrow(() -> new EntityNotFoundException("No webhook found for the user"));

        // Update the URL
        webhook.setCallbackUrl(newUrl);

        // Save the updated webhook
        webhookRepository.save(webhook);

        LOG.info("Webhook URL for user {} updated to {}", username, newUrl);
    }

    @Override
    @Transactional
    public void triggerBatchWebhookForUser(String username) {
        // Get the user's webhook
        Webhook webhook = webhookRepository.findByApplicationUser(applicationUserRepository.findApplicationUserByUsername(username))
                .orElseThrow(() -> new EntityNotFoundException("No webhook found for the user"));

        // Get products associated with the webhook
        Set<Product> products = webhook.getProducts();
        if (products == null || products.isEmpty()) {
            throw new EntityNotFoundException("No products associated with the webhook");
        }

        // Prepare the batch payload
        List<Map<String, Object>> productData = products.stream()
                .map(product -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("GTIN", product.getGtin());
                    return map;
                })
                .toList();

        Map<String, Object> payload = Map.of(
                "event", "trigger_webhook",
                "registered_products", productData,
                "timestamp", LocalDateTime.now().toString() // ISO-8601 timestamp
        );

        // Send the webhook
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhook.getCallbackUrl(), payload, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOG.info("Successfully sent batch webhook to {}", webhook.getCallbackUrl());
            } else {
                LOG.error("Failed to send webhook. Status code: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            LOG.error("Error sending batch webhook to {}: {}", webhook.getCallbackUrl(), e.getMessage());
            throw new UrlNotReachableException("Failed to send webhook");
        }
    }

    @KafkaListener(
            id = "priceAdjustmentListener",
            groupId = "trigger.price.adjustment",
            topics = "${kafka.topic.pricingRuleAdjustment:pricing.rule.adjustment}"
    )
    @Override
    @Transactional
    public void triggerWebhookForPriceAdjustmentDto(PriceAdjustmentDto priceAdjustmentDto) {
        // Get the user
        ApplicationUser applicationUser = applicationUserRepository.findById(priceAdjustmentDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("No user found with ID: " + priceAdjustmentDto.getUserId()));

        // Get the user's webhook
        Webhook webhook = webhookRepository.findByApplicationUser(applicationUser)
                .orElseThrow(() -> new EntityNotFoundException("No webhook found for the user"));

        // Fetch the product by its ID to get the GTIN
        Product product = productRepository.findById(priceAdjustmentDto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("No product found with ID: " + priceAdjustmentDto.getProductId()));

        PricingRule pricingRule = pricingRuleRepository.findById(priceAdjustmentDto.getPricingRuleId())
                .orElseThrow(() -> new EntityNotFoundException("No pricing rule found with ID: " + priceAdjustmentDto.getPricingRuleId()));


        Map<String, Object> productData = new HashMap<>();
        productData.put("GTIN", product.getGtin());
        productData.put("pricingRule", pricingRule.getName());
        productData.put("adjustedPrice", priceAdjustmentDto.getAdjustedPrice());
        productData.put("averagePrice", priceAdjustmentDto.getAveragePrice());
        productData.put("cheapestPrice", priceAdjustmentDto.getCheapestPrice());
        productData.put("highestPrice", priceAdjustmentDto.getHighestPrice());
        productData.put("adjustmentDate", priceAdjustmentDto.getAdjustmentDate().toString()); // ISO-8601 format

        Map<String, Object> payload = Map.of(
                "event", "priceChange",
                "product", productData, // Single product data
                "timestamp", priceAdjustmentDto.getAdjustmentDate() // timestamp
        );


        // Send the webhook
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhook.getCallbackUrl(), payload, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOG.info("Successfully sent price adjustment webhook to {}", webhook.getCallbackUrl());
            } else {
                LOG.error("Failed to send webhook. Status code: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            LOG.error("Error sending price adjustment webhook to {}: {}", webhook.getCallbackUrl(), e.getMessage());
            throw new UrlNotReachableException("Failed to send webhook");
        }
    }


}
