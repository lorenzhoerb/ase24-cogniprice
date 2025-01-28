package ase.cogniprice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "webhook")
@Data
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "callback_url", nullable = false, length = 2056)
    private String callbackUrl;

    @Column(name = "secret")
    private String secret;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Many-to-Many relationship with Product
    @ManyToMany(mappedBy = "webhooks", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Product> products = new HashSet<>();

    // One-to-One optional relationship with User
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "application_user_id", unique = true)
    private ApplicationUser applicationUser;
}
