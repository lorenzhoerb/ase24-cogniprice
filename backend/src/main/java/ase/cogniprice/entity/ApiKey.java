package ase.cogniprice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_key")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "applicationUser")
public class ApiKey {
    // @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Encrypted API Key
    @Column(name = "access_key", nullable = false, unique = true)
    private String accessKey;

    // Masked version of the API key for display
    @Column(name = "masked_key", nullable = false)
    private String maskedKey;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_user_id", nullable = false, unique = true)
    private ApplicationUser applicationUser;


    public boolean isExpired() {
        return this.expiresAt.isBefore(LocalDateTime.now());
    }

    public void setMaskedKey(String accessKey) {
        this.maskedKey = accessKey.substring(0, 4) + "*".repeat(accessKey.length() - 8) + accessKey.substring(accessKey.length() - 4);
    }
}
