package ase.cogniprice.entity;

import ase.cogniprice.controller.dto.pricing.rule.PositionDto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pricing_rule")
@Data
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "application_user_id", nullable = false)
    private ApplicationUser applicationUser; // Owner of the rule

    @Embedded
    private Position position;

    @Enumerated(EnumType.STRING)
    private Scope scope; // PRODUCT or CATEGORY

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "min_limit_id", referencedColumnName = "id")
    private PriceLimit minLimit;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "max_limit_id", referencedColumnName = "id")
    private PriceLimit maxLimit;

    @ManyToMany
    @JoinTable(
            name = "pricing_rule_category",
            joinColumns = @JoinColumn(name = "pricing_rule_id"),
            inverseJoinColumns = @JoinColumn(name = "product_category_id")
    )
    private Set<ProductCategory> categories = new HashSet<>(); // Applicable for CATEGORY scope

    @ManyToMany
    @JoinTable(
            name = "pricing_rule_product",
            joinColumns = @JoinColumn(name = "pricing_rule_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>(); // Applicable for PRODUCT scope

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public void addProduct(Product product) {
        products.add(product);
    }

    public void addCategory(ProductCategory category) {
        categories.add(category);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class Position {
        @Column(name = "position_value")
        private BigDecimal value;

        @Enumerated(EnumType.STRING)
        @Column(name = "position_unit")
        private Unit unit;

        @Enumerated(EnumType.STRING)
        @Column(name = "position_match_type")
        private MatchType matchType;

        @Enumerated(EnumType.STRING)
        @Column(name = "position_reference")
        private Reference reference;

        public static Position from(PositionDto positionDto) {
            return new Position(positionDto.getValue(), positionDto.getUnit(), positionDto.getMatchType(), positionDto.getReference());
        }


        public enum Unit {
            EUR,
            PERCENTAGE
        }

        public enum MatchType {
            EQUALS,
            HIGHER,
            LOWER
        }

        public enum Reference {
            CHEAPEST,
            AVERAGE,
            HIGHEST
        }
    }

    public enum Scope {
        PRODUCT,
        CATEGORY
    }
}


