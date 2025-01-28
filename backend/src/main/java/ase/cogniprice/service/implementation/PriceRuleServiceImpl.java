package ase.cogniprice.service.implementation;

import ase.cogniprice.controller.dto.pricing.rule.PositionDto;
import ase.cogniprice.controller.dto.pricing.rule.PriceLimitDto;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleDetailsDto;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleListFilter;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleRequestDto;
import ase.cogniprice.controller.dto.pricing.rule.PricingRuleStatus;
import ase.cogniprice.controller.dto.pricing.rule.SimplePricingRuleDto;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.PriceLimit;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.exception.ConflictException;
import ase.cogniprice.exception.ForbiddenException;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.PricingRuleRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.service.PriceRuleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class PriceRuleServiceImpl implements PriceRuleService {

    private final ApplicationUserRepository applicationUserRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;

    public PriceRuleServiceImpl(
            ApplicationUserRepository applicationUserRepository,
            PricingRuleRepository pricingRuleRepository, ProductCategoryRepository productCategoryRepository, ProductRepository productRepository) {
        this.applicationUserRepository = applicationUserRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Page<SimplePricingRuleDto> getPriceRules(String username, PriceRuleListFilter filter, Pageable pageable) {
        log.trace("getPriceRules({}, {}, {})", username, filter, pageable);
        ApplicationUser user = getAuthenticatedUser(username);

        // Set default filter if null
        PriceRuleListFilter appliedFilter = filter != null ? filter : new PriceRuleListFilter(null, null);

        // Convert filter status to active flag
        Boolean isActive = appliedFilter.status() != null ? appliedFilter.status() == PricingRuleStatus.ACTIVE : null;

        String priceRuleName = appliedFilter.name() != null ? appliedFilter.name() : "";

        // Fetch the pricing rules based on the filter and user ID
        Page<PricingRule> priceRule = pricingRuleRepository.findPricingRulesByFilter(
                user.getId(), priceRuleName, isActive, pageable);

        // Map and return as Page of SimplePricingRuleDto
        return priceRule.map(this::mapPriceRule);
    }

    @Override
    public void deletePriceRuleById(String username, Long priceRuleId) {
        log.trace("deletePricingRuleById({}, {})", priceRuleId, username);
        ApplicationUser user = getAuthenticatedUser(username);
        PricingRule pricingRule = getPricingRuleOrElseNotFound(priceRuleId);

        if (!isOwner(user, pricingRule)) {
            throw new ForbiddenException("You are not allowed to delete this rule");
        }

        pricingRuleRepository.delete(pricingRule);
    }

    @Override
    public PriceRuleDetailsDto createPriceRule(String username, PriceRuleRequestDto priceRuleRequest) {
        log.trace("createPriceRule({}, {})", username, priceRuleRequest);
        ApplicationUser user = getAuthenticatedUser(username);
        validateRequest(priceRuleRequest); // validate position and limit invariants

        PricingRule pricingRule = createBasicPriceRule(user, priceRuleRequest);

        // Handle categories and products based on the scope
        handleScopeItems(priceRuleRequest, pricingRule, username);

        PricingRule createdPricingRule = pricingRuleRepository.save(pricingRule);

        return PriceRuleDetailsDto.fromPricingRule(createdPricingRule);
    }

    @Override
    public PriceRuleDetailsDto updatePriceRule(String username, Long priceRuleId, PriceRuleRequestDto priceRuleRequest) {
        log.trace("updatePriceRule({}, {}, {})", username, priceRuleId, priceRuleRequest);
        validateRequest(priceRuleRequest); // validate position and limit invariants
        ApplicationUser user = getAuthenticatedUser(username);
        PricingRule pricingRule = getPricingRuleOrElseNotFound(priceRuleId);
        if (!isOwner(user, pricingRule)) {
            throw new ForbiddenException("You are not update to delete this rule");
        }

        pricingRule.getCategories().clear();
        pricingRule.getProducts().clear();
        handleUpdatePriceRule(pricingRule, createBasicPriceRule(user, priceRuleRequest));
        handleScopeItems(priceRuleRequest, pricingRule, username);

        PricingRule updatedPricingRule = pricingRuleRepository.save(pricingRule);

        return PriceRuleDetailsDto.fromPricingRule(updatedPricingRule);
    }


    @Override
    public PriceRuleDetailsDto getPriceRuleById(String username, Long priceRuleId) {
        log.trace("getPriceRuleById({}, {})", username, priceRuleId);
        ApplicationUser user = getAuthenticatedUser(username);
        PricingRule pricingRule = getPricingRuleOrElseNotFound(priceRuleId);
        if (!isOwner(user, pricingRule)) {
            throw new ForbiddenException("You are not allowed to perform this operation");
        }
        return PriceRuleDetailsDto.fromPricingRule(pricingRule);
    }

    // --- Helper Methods for CRUD Operations ---

    private void handleScopeItems(PriceRuleRequestDto priceRuleRequest, PricingRule pricingRule, String username) {
        if (priceRuleRequest.getScope() == PricingRule.Scope.CATEGORY) {
            addCategories(priceRuleRequest, pricingRule);
        } else if (priceRuleRequest.getScope() == PricingRule.Scope.PRODUCT) {
            addProducts(priceRuleRequest, pricingRule, username);
        }
    }

    private void addProducts(PriceRuleRequestDto priceRuleRequest, PricingRule pricingRule, String username) {
        for (String productStringId : priceRuleRequest.getAppliedToIds()) {
            try {
                Long productId = Long.parseLong(productStringId);
                Product product = productRepository.findProductOfUserById(username, productId)
                        .orElseThrow(() -> new ConflictException("Product with id " + productStringId + " could not be added"));
                pricingRule.addProduct(product);
            } catch (NumberFormatException e) {
                throw new ConflictException("Product with id " + productStringId + " could not be added");
            }
        }
    }

    private void addCategories(PriceRuleRequestDto priceRuleRequest, PricingRule pricingRule) {
        for (String categoryId : priceRuleRequest.getAppliedToIds()) {
            ProductCategory productCategory = productCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ConflictException("Category with id " + categoryId + " could not be added"));
            pricingRule.addCategory(productCategory);
        }
    }

    private void handleUpdatePriceRule(PricingRule pricingRule, PricingRule basicPriceRule) {
        pricingRule.setName(basicPriceRule.getName());
        pricingRule.setActive(basicPriceRule.isActive());
        if (basicPriceRule.getMaxLimit() == null) {
            pricingRule.setMaxLimit(null);
        } else {
            pricingRule.setMaxLimit(basicPriceRule.getMaxLimit());
        }

        if (basicPriceRule.getMinLimit() == null) {
            pricingRule.setMinLimit(null);
        } else {
            pricingRule.setMinLimit(basicPriceRule.getMinLimit());
        }
        pricingRule.setScope(basicPriceRule.getScope());
        pricingRule.setPosition(basicPriceRule.getPosition());
    }

    /**
     * Create basic pricing rule without scope entities.
     *
     * @param user             user
     * @param priceRuleRequest price rule request
     * @return price rule entity
     */
    private static PricingRule createBasicPriceRule(ApplicationUser user, PriceRuleRequestDto priceRuleRequest) {
        PricingRule pricingRule = new PricingRule();
        pricingRule.setName(priceRuleRequest.getName());
        pricingRule.setActive(priceRuleRequest.getIsActive());
        pricingRule.setApplicationUser(user);
        pricingRule.setMinLimit(PriceLimit.from(priceRuleRequest.getMinLimit()));
        pricingRule.setMaxLimit(PriceLimit.from(priceRuleRequest.getMaxLimit()));
        pricingRule.setScope(priceRuleRequest.getScope());
        pricingRule.setPosition(PricingRule.Position.from(priceRuleRequest.getPosition()));
        return pricingRule;
    }

    private PricingRule getPricingRuleOrElseNotFound(Long pricingRuleId) {
        return pricingRuleRepository.findById(pricingRuleId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pricing rule with id '" + pricingRuleId + "' not found."));
    }

    // --- Validation Methods ---

    private void validateRequest(PriceRuleRequestDto priceRuleRequest) {
        validatePosition(priceRuleRequest.getPosition());
        validateLimits(priceRuleRequest.getMinLimit(), priceRuleRequest.getMaxLimit());
    }

    private void validateLimits(PriceLimitDto minLimit, PriceLimitDto maxLimit) {
        if (minLimit != null && minLimit.getLimitType() == PriceLimit.LimitType.FIXED_AMOUNT &&
                maxLimit != null && maxLimit.getLimitType() == PriceLimit.LimitType.FIXED_AMOUNT) {
            if (minLimit.getLimitValue().compareTo(maxLimit.getLimitValue()) > 0) {
                throw new ConflictException("Min limit must be smaller than max limit.");
            }
        }
    }

    private void validatePosition(PositionDto position) {
        if (position == null) {
            return; // If no position is provided, no validation needed
        }

        // Handling the EQUALS match type case
        if (position.getMatchType() == PricingRule.Position.MatchType.EQUALS) {
            if (position.getUnit() != null) {
                throw new ConflictException("For position type 'EQUALS', unit must not be provided.");
            }
            if (position.getValue() != null) {
                throw new ConflictException("For position type 'EQUALS', value must not be provided.");
            }
        } else {
            // Handling other match types (LOWER and HIGHER)
            if (position.getUnit() == null) {
                throw new ConflictException("For position type '" + position.getMatchType() + "', unit must be provided.");
            }
            if (position.getValue() == null) {
                throw new ConflictException("For position type '" + position.getMatchType() + "', value must be provided.");
            }
        }
    }

    // --- Helper Methods for Authentication and Ownership ---

    private boolean isOwner(ApplicationUser user, PricingRule pricingRule) {
        return user.getId().equals(pricingRule.getApplicationUser().getId());
    }


    private ApplicationUser getAuthenticatedUser(String username) {
        ApplicationUser user = applicationUserRepository.findApplicationUserByUsername(username);
        if (user == null) {
            log.warn("User with username '{}' not found.", username);
            throw new EntityNotFoundException("User with username '" + username + "' not found.");
        }
        return user;
    }

    // --- Utility Methods ---

    private SimplePricingRuleDto mapPriceRule(PricingRule pricingRule) {
        if (pricingRule == null) {
            return null;
        }

        // Extract the Position object
        PricingRule.Position position = pricingRule.getPosition();

        // Create and return the SimplePricingRuleDto
        return new SimplePricingRuleDto(
                pricingRule.getId(),
                pricingRule.getName(),
                pricingRule.isActive(),
                position != null ? position.getMatchType() : null,
                position != null ? position.getReference() : null,
                position != null ? position.getValue() : null,
                position != null ? position.getUnit() : null,
                pricingRule.getScope(),
                pricingRule.getMaxLimit() != null, // Check if maxLimit exists
                pricingRule.getMinLimit() != null  // Check if minLimit exists
        );
    }
}
