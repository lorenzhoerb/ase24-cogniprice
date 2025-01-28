package ase.cogniprice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {

    private static final int EXPIRATION = 15; //token is valid 15 min

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = ApplicationUser.class)
    @JoinColumn(nullable = false, name = "user_id")
    private ApplicationUser user;

    private Date expiryDate;

    public PasswordResetToken(ApplicationUser user, String token) {
        this.token = token;
        this.user = user;
        this.expiryDate = Date.from(
            LocalDateTime.now()
                .plusMinutes(EXPIRATION)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );
    }

    public void update(String token) {
        this.token = token;
        this.expiryDate = Date.from(
            LocalDateTime.now()
                .plusMinutes(EXPIRATION)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );
    }

    public boolean isExpired() {
        Calendar cal = Calendar.getInstance();
        return this.expiryDate.before(cal.getTime());
    }
}