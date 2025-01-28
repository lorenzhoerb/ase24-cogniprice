package ase.cogniprice.entity;

import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.type.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "application_user")
@Data //combines @Getter, @Setter, @ToString, @EqualsAndHashCode, and @RequiredArgsConstructor
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "login_attempts", nullable = false)
    private Long loginAttempts;

    @Column(name = "user_locked", nullable = false)
    private boolean userLocked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // One-to-many relationship with Notification

    @OneToMany(mappedBy = "applicationUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Notification> notifications = new HashSet<>();

    // One-to-one relationship with ApiKey
    @OneToOne(mappedBy = "applicationUser", cascade = CascadeType.ALL, orphanRemoval = true, optional = true, fetch = FetchType.LAZY)
    private ApiKey apiKey;

    // Many-to-Many relationship with StoreProduct
    @ManyToMany()
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinTable(
        name = "user_store_product",
        joinColumns = @JoinColumn(name = "application_user_id"),
        inverseJoinColumns = {
            @JoinColumn(name = "competitor_id", referencedColumnName = "competitor_id"),
            @JoinColumn(name = "product_id", referencedColumnName = "product_id")
        }
    )
    private Set<StoreProduct> storeProducts = new HashSet<>();


    @OneToMany(mappedBy = "applicationUser",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ProductUserPricingRule> productUserPricingRules = new HashSet<>();

    @OneToMany(mappedBy = "applicationUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<PricingRule> pricingRules = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private PasswordResetToken passwordResetToken;

    public void incrementLoginAttempts() {
        this.setLoginAttempts(this.getLoginAttempts() + 1);
    }

    public boolean removeStoreProduct(StoreProduct storeProduct) {
        return this.storeProducts.remove(storeProduct);
    }

    public boolean addStoreProduct(StoreProduct storeProduct, StoreProductRepository storeProductRepository) {
        boolean added = this.storeProducts.add(storeProduct);
        // Ensure bidirectional consistency
        if (added) {

            Set<ApplicationUser> users = storeProduct.getApplicationUsers();
            users.add(this);
            storeProduct.setApplicationUsers(users);
            storeProductRepository.save(storeProduct);
        }
        return added;
    }

}
