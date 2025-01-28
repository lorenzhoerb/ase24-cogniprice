package ase.cogniprice.controller;

import ase.cogniprice.controller.dto.pricing.rule.PriceRuleDetailsDto;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleListFilter;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleRequestDto;
import ase.cogniprice.controller.dto.pricing.rule.PricingRuleStatus;
import ase.cogniprice.controller.dto.pricing.rule.SimplePricingRuleDto;
import ase.cogniprice.service.PriceRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/pricing-rules")
public class PricingRuleController {

    private final PriceRuleService priceRuleService;

    public PricingRuleController(PriceRuleService priceRuleService) {
        this.priceRuleService = priceRuleService;
    }

    @Secured({"ROLE_USER"})
    @GetMapping
    @Operation(
            summary = "Get all pricing rules for a user",
            description = "Fetch all pricing rules for a specific user, with optional filters for status and name.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully fetched pricing rules",
                        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @Schema(implementation = SimplePricingRuleDto.class))),
                @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized")
            }
    )
    public Page<SimplePricingRuleDto> getAllPricingRulesForUser(
            @AuthenticationPrincipal String username,
            @RequestParam(required = false) PricingRuleStatus status,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("Fetching pricing rules for user: {}, status: {}, name: {}, pageable: {}", username, status, name, pageable);
        return priceRuleService.getPriceRules(username, new PriceRuleListFilter(name, status), pageable);
    }

    @Secured({"ROLE_USER"})
    @GetMapping("/{id}")
    @Operation(
            summary = "Get a pricing rule by ID",
            description = "Get a pricing rule identified by the provided ID.",
            responses = {
                @ApiResponse(
                    responseCode = "200",
                    description = "Successfully get the pricing rule",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PriceRuleDetailsDto.class))
                    ),
                @ApiResponse(responseCode = "403", description = "Forbidden - User does not own the pricing rule"),
                @ApiResponse(responseCode = "404", description = "Pricing rule not found")
            }
    )
    public PriceRuleDetailsDto getPricingRuleById(@PathVariable("id") Long id, @AuthenticationPrincipal String username) {
        log.info("Fetching pricing rule by ID: {}", id);
        return priceRuleService.getPriceRuleById(username, id);
    }


    @Secured({"ROLE_USER"})
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a pricing rule by ID",
            description = "Deletes a pricing rule identified by the provided ID.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully deleted the pricing rule"),
                @ApiResponse(responseCode = "403", description = "Forbidden - User does not own the pricing rule"),
                @ApiResponse(responseCode = "404", description = "Pricing rule not found")
            }
    )
    public ResponseEntity<?> deletePricingRuleById(@PathVariable("id") Long id, @AuthenticationPrincipal String username) {
        priceRuleService.deletePriceRuleById(username, id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @Secured({"ROLE_USER"})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new pricing rule",
            description = """
                    Allows the authenticated user to create a pricing rule by specifying details such as name, scope,\s
                    applied entities (e.g., products or categories), position, and price limits. The rule is created\s
                    with the specified configuration, and validation checks are applied to ensure logical consistency\s
                    and prevent conflicts.
                    
                    Validation includes:
                    - Ensuring the applied entity (product ID or category) is valid and accessible by the user.
                    - Verifying the consistency of configuration combinations (e.g., match types and units).
                    - Ensuring the minimum limit is lower than the maximum limit, if both are set.
                    """,
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Successfully created the pricing rule",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = PriceRuleDetailsDto.class)
                        )
                    ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid input data",
                        content = @Content(
                                mediaType = "application/json"
                        )
                    ),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - User not authorized"
                    ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Conflict in price rule configuration",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(example = "{\"message\": \"Conflict error\", \"details\": [\"The product ID is not valid\"]}")
                        )
                    ),
            }
    )
    public PriceRuleDetailsDto createPricingRule(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody PriceRuleRequestDto priceRuleRequest) {
        log.info("Creating a pricing rule: {}", priceRuleRequest);
        return priceRuleService.createPriceRule(username, priceRuleRequest);
    }

    @Secured({"ROLE_USER"})
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update a new pricing rule",
            description = """
                    Allows the authenticated user to create a pricing rule by specifying details such as name, scope,\s
                    applied entities (e.g., products or categories), position, and price limits. The rule is created\s
                    with the specified configuration, and validation checks are applied to ensure logical consistency\s
                    and prevent conflicts.
                    
                    Validation includes:
                    - Ensuring the applied entity (product ID or category) is valid and accessible by the user.
                    - Verifying the consistency of configuration combinations (e.g., match types and units).
                    - Ensuring the minimum limit is lower than the maximum limit, if both are set.
                    """,
            responses = {
                @ApiResponse(
                         responseCode = "200",
                         description = "Successfully updated the pricing rule",
                         content = @Content(
                             mediaType = "application/json",
                             schema = @Schema(implementation = PriceRuleDetailsDto.class)
                             )
                    ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid input data",
                        content = @Content(
                                    mediaType = "application/json"
                        )
                    ),
                @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User not authorized"
                    ),
                @ApiResponse(
                            responseCode = "409",
                            description = "Conflict in price rule configuration",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"message\": \"Conflict error\", \"details\": [\"The product ID is not valid\"]}")
                            )
                    ),
                @ApiResponse(
                            responseCode = "404",
                            description = "The pricing rule with id not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"message\": \"Conflict error\", \"details\": [\"The product ID is not valid\"]}")
                            )
                    ),
            }
    )
    public PriceRuleDetailsDto updatePricingRule(
            @AuthenticationPrincipal String username,
            @PathVariable("id") Long priceRuleId,
            @Valid @RequestBody PriceRuleRequestDto priceRuleRequest) {
        log.info("Updating a pricing {} rule: {}", priceRuleId, priceRuleRequest);
        return priceRuleService.updatePriceRule(username, priceRuleId, priceRuleRequest);
    }
}
