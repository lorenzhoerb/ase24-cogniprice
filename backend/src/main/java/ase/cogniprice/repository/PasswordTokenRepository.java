package ase.cogniprice.repository;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByUser(ApplicationUser user);

    PasswordResetToken findByToken(String token);
}
