package ase.cogniprice.controller;

import ase.cogniprice.controller.dto.webhook.dto.WebhookRegistrationDto;
import ase.cogniprice.controller.mapper.WebhookMapper;
import ase.cogniprice.entity.Webhook;
import ase.cogniprice.exception.InvalidUrlException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.UrlNotReachableException;
import ase.cogniprice.exception.UsernameAlreadyExistsException;
import ase.cogniprice.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

@RestController
@Slf4j
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final WebhookService webhookService;
    private final WebhookMapper webhookMapper;

    @Autowired
    public WebhookController(WebhookService webhookService, WebhookMapper webhookMapper) {
        this.webhookService = webhookService;
        this.webhookMapper = webhookMapper;
    }

    @Secured("ROLE_USER")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new Webhook")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Webhook created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "409", description = "Webhook already exists for the user")
    })
    public ResponseEntity<String> registerWebhook(
            @Valid @RequestBody WebhookRegistrationDto webhookRegistrationDto,
            @AuthenticationPrincipal String username
    ) {
        LOG.info("User {} is registering a webhook with url: {}", username, webhookRegistrationDto);

        String secret = webhookRegistrationDto.getSecret();
        if (secret == null || secret.isEmpty()) {
            LOG.info("No secret provided for webhook registration.");
        } else {
            LOG.info("Secret provided: {}", secret);
        }

        webhookService.registerWebhook(webhookRegistrationDto, username);

        return new ResponseEntity<>("Sucessfully created Webhook!",
                HttpStatus.CREATED);
    }

    @Secured("ROLE_USER")
    @PostMapping("/add-product/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Add a product to the user's webhook")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product added to webhook successfully"),
        @ApiResponse(responseCode = "404", description = "Webhook or product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user")
    })
    public ResponseEntity<String> addProductToWebhook(
            @PathVariable Long productId,
            @AuthenticationPrincipal String username
    ) {
        webhookService.addProductToWebhook(productId, username);
        return new ResponseEntity<>("Product added to webhook successfully", HttpStatus.OK);
    }

    @Secured("ROLE_USER")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete the user's webhook")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Webhook deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Webhook not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user")
    })
    public ResponseEntity<String> deleteWebhook(@AuthenticationPrincipal String username) {
        LOG.info("User {} is deleting their webhook", username);
        webhookService.deleteWebhook(username);
        return new ResponseEntity<>("Webhook deleted successfully", HttpStatus.NO_CONTENT);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/remove-product/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove a product from the user's webhook")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product removed from webhook successfully"),
        @ApiResponse(responseCode = "404", description = "Webhook or product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user")
    })
    public ResponseEntity<String> removeProductFromWebhook(
            @PathVariable Long productId,
            @AuthenticationPrincipal String username
    ) {
        LOG.info("User {} is removing product ID: {} from their webhook", username, productId);

        webhookService.removeProductFromWebhook(productId, username);
        return new ResponseEntity<>("Product removed from webhook successfully", HttpStatus.OK);
    }

    @Secured("ROLE_USER")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get the user's webhook")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Webhook details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Webhook not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user")
    })
    public ResponseEntity<WebhookRegistrationDto> getWebhook(@AuthenticationPrincipal String username) {
        LOG.info("Fetching webhook for user {}", username);

        Webhook webhook = webhookService.getWebhook(username);
        WebhookRegistrationDto webhookDto = webhookMapper.toDto(webhook);
        return new ResponseEntity<>(webhookDto, HttpStatus.OK);

    }

    @Secured("ROLE_USER")
    @PutMapping("/update-url")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update the webhook URL")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Webhook URL updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid URL provided"),
        @ApiResponse(responseCode = "404", description = "Webhook not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user")
    })
    public ResponseEntity<String> updateWebhookUrl(
            @RequestBody WebhookRegistrationDto webhookDto,
            @AuthenticationPrincipal String username
    ) {
        LOG.info("Updating webhook URL for user {} to {}", username, webhookDto.getCallbackUrl());

        webhookService.updateWebhookUrl(webhookDto.getCallbackUrl(), username);
        return new ResponseEntity<>("Webhook URL updated successfully", HttpStatus.OK);
    }

    @Secured("ROLE_USER")
    @PostMapping("/send-data")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Trigger sending data to registered webhooks")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Webhook data sent successfully"),
        @ApiResponse(responseCode = "404", description = "Webhook not found or no associated products"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user")
    })
    public ResponseEntity<String> sendWebhookData(@AuthenticationPrincipal String username) {
        LOG.info("User {} is triggering webhook data sending", username);

        webhookService.triggerBatchWebhookForUser(username);
        return new ResponseEntity<>("Webhook data sent successfully", HttpStatus.OK);
    }


}
