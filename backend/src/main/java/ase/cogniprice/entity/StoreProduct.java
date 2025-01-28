package ase.cogniprice.entity;

import ase.cogniprice.type.CrawlState;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "store_product", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "competitor_id"})
})
@Data
public class StoreProduct {

    @EmbeddedId
    private StoreProductId id;

    @Column(name = "product_url", nullable = false, length = 2056)
    private String productUrl;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @MapsId("competitorId")
    @JoinColumn(name = "competitor_id", nullable = false)
    private Competitor competitor;

    @Column(name = "crawl_interval", nullable = false)
    private Duration interval;

    @Column(name = "last_crawled", nullable = false)
    private ZonedDateTime lastCrawled;

    @Column(name = "next_crawl", nullable = false)
    private ZonedDateTime nextCrawl;

    @Enumerated(EnumType.STRING)
    @Column(name = "crawl_state", nullable = false)
    private CrawlState crawlState;

    @Column(name = "retry_attempts")
    private Long retryAttempts;

    @Column(name = "pause_requested", nullable = false)
    private Boolean pauseRequested;

    @ManyToMany(mappedBy = "storeProducts")
    private Set<ApplicationUser> applicationUsers = new HashSet<>();

    @OneToMany(mappedBy = "id.storeProductId")  // Update to match the relationship in ProductPrice
    private Set<ProductPrice> prices = new HashSet<>();

    public void updateNextCrawl() {
        ZonedDateTime now = ZonedDateTime.now();
        if (lastCrawled.plus(interval).isAfter(now)) {
            nextCrawl = lastCrawled.plus(interval);
        } else {
            nextCrawl = now;
        }
    }

    public boolean addApplicationUser(ApplicationUser applicationUser) {

        boolean added = this.applicationUsers.add(applicationUser);
        // Ensure bidirectional consistency
        if (added) {
            applicationUser.getStoreProducts().add(this);
        }
        return added;

    }


    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreProductId implements Serializable {
        @Column(name = "product_id")
        private Long productId;

        @Column(name = "competitor_id")
        private Long competitorId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StoreProductId that = (StoreProductId) o;
            return Objects.equals(productId, that.productId) &&
                Objects.equals(competitorId, that.competitorId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, competitorId);
        }

        @Override
        public String toString() {
            return productId + ":" + competitorId;
        }
    }

}
