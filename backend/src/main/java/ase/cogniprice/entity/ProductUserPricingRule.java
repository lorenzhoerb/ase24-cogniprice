package ase.cogniprice.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "product_user_pricing_rule", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "application_user_id"}),
    @UniqueConstraint(columnNames = {"application_user_id", "pricing_rule_id"})
})
@Data
public class ProductUserPricingRule {

    @EmbeddedId
    private ProductUserPricingRuleId id;

    // Many-to-One relationship with Product
    @ManyToOne
    @MapsId("productId")
    private Product product;

    // Many-to-One relationship with User
    @ManyToOne
    @MapsId("applicationUserId")
    private ApplicationUser applicationUser;

    // Many-to-One relationship with PricingRule
    @ManyToOne
    @MapsId("pricingRuleId")
    @JoinColumn(name = "pricingRuleId", nullable = true)
    private PricingRule pricingRule;

    @Embeddable
    @Data
    public static class ProductUserPricingRuleId implements Serializable {
        private Long productId;
        private Long applicationUserId;
        private Long pricingRuleId;

        public ProductUserPricingRuleId() {}

        public ProductUserPricingRuleId(Long productId, Long applicationUserId, Long pricingRuleId) {
            this.productId = productId;
            this.applicationUserId = applicationUserId;
            this.pricingRuleId = pricingRuleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ProductUserPricingRuleId that = (ProductUserPricingRuleId) o;
            return Objects.equals(productId, that.productId) &&
                    Objects.equals(applicationUserId, that.applicationUserId) &&
                    Objects.equals(pricingRuleId, that.pricingRuleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, applicationUserId, pricingRuleId);
        }

    }
}
