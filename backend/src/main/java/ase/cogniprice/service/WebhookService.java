package ase.cogniprice.service;

import ase.cogniprice.controller.dto.pricing.rule.PriceAdjustmentDto;
import ase.cogniprice.controller.dto.webhook.dto.WebhookRegistrationDto;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Webhook;
import ase.cogniprice.exception.InvalidUrlException;
import ase.cogniprice.exception.UrlNotReachableException;
import jakarta.persistence.EntityNotFoundException;

public interface WebhookService {


    /**
     * Registers a new webhook for the specified user.
     *
     * @param webhookRegistrationDto the DTO containing the webhook registration details
     * @param username               the username of the application user
     * @throws InvalidUrlException      if the provided callback URL is invalid
     * @throws UrlNotReachableException if the provided callback URL is not reachable
     */
    void registerWebhook(WebhookRegistrationDto webhookRegistrationDto, String username);

    /**
     * Associates a product with an existing webhook for the specified user.
     *
     * @param productId the ID of the product to associate
     * @param username  the username of the application user
     * @throws EntityNotFoundException if the webhook or product cannot be found
     */
    void addProductToWebhook(Long productId, String username);

    /**
     * Deletes the webhook associated with the specified user.
     *
     * @param username the username of the application user
     * @throws EntityNotFoundException if no webhook is found for the user
     */
    void deleteWebhook(String username);

    /**
     * Removes a product association from an existing webhook for the specified user.
     *
     * @param productId the ID of the product to remove
     * @param username  the username of the application user
     * @throws EntityNotFoundException if the webhook or product cannot be found
     * @throws IllegalArgumentException if the product association does not exist
     */
    void removeProductFromWebhook(Long productId, String username);

    /**
     * Retrieves the webhook associated with the specified user.
     *
     * @param username the username of the application user
     * @return the {@link Webhook} entity associated with the user
     * @throws EntityNotFoundException if no webhook is found for the user
     */
    Webhook getWebhook(String username);

    /**
     * Updates the callback URL of the webhook associated with the specified user.
     *
     * @param newUrl   the new callback URL for the webhook
     * @param username the username of the application user
     * @throws InvalidUrlException      if the new callback URL is invalid
     * @throws UrlNotReachableException if the new callback URL is not reachable
     * @throws EntityNotFoundException  if no webhook is found for the user
     */
    void updateWebhookUrl(String newUrl, String username);

    /**
     * Triggers a batch webhook event for the specified user, notifying them of product changes.
     *
     * @param username the username of the application user
     * @throws EntityNotFoundException if no webhook or associated products are found for the user
     * @throws UrlNotReachableException if the callback URL is unreachable
     */
    void triggerBatchWebhookForUser(String username);

    /**
     * Triggers a webhook for a price adjustment event.
     * This method sends a webhook notification based on price adjustment information passed in the PriceAdjustmentDto.
     * It sends updates regarding price changes to the appropriate webhook.
     *
     * @param priceAdjustmentDto the DTO containing price adjustment details
     */
    void triggerWebhookForPriceAdjustmentDto(PriceAdjustmentDto priceAdjustmentDto);
}
