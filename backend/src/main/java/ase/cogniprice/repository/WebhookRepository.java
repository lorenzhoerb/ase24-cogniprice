package ase.cogniprice.repository;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    Optional<Webhook> findByApplicationUser(ApplicationUser user);
    // Additional custom query methods can be added here if needed

}
