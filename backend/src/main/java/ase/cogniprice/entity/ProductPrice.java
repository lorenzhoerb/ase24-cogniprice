package ase.cogniprice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.javamoney.moneta.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "product_price")
@Data
public class ProductPrice {

    @EmbeddedId
    private ProductPriceId id;

    @ManyToOne
    @MapsId("storeProductId")  // This maps to the composite key of StoreProduct
    @JoinColumns({
        @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false),
        @JoinColumn(name = "competitor_id", referencedColumnName = "competitor_id", nullable = false)
    })
    private StoreProduct storeProduct;

    @Column(name = "price", nullable = false)
    @Positive
    @NotNull
    private BigDecimal price;

    @Column(name = "currency", nullable = false)
    @NotNull
    private String currency;


    // Helper method to set Money as price and currency automatically
    public void setPrice(Money money) {
        if (money != null) {
            this.price = money.getNumber().numberValue(BigDecimal.class);
            this.currency = money.getCurrency().getCurrencyCode();
        }
    }

    public Money getPrice() {
        return Money.of(price, currency);
    }

    @Embeddable
    @Data
    public static class ProductPriceId implements Serializable {

        @Embedded
        @JoinColumns({
            @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false),
            @JoinColumn(name = "competitor_id", referencedColumnName = "competitor_id", nullable = false)
        })
        private StoreProduct.StoreProductId storeProductId;  // Reference the composite ID

        @Column(name = "price_time", nullable = false)
        private LocalDateTime priceTime;

        public ProductPriceId() {
        }

        // Parameterized constructor
        public ProductPriceId(StoreProduct.StoreProductId storeProductId, LocalDateTime timestamp) {
            this.storeProductId = storeProductId;
            this.priceTime = timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ProductPriceId that = (ProductPriceId) o;
            return Objects.equals(storeProductId, that.storeProductId) &&
                Objects.equals(priceTime, that.priceTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(storeProductId, priceTime);
        }
    }

}


