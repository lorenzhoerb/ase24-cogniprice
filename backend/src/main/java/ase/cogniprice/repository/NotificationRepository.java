package ase.cogniprice.repository;

import ase.cogniprice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Additional query methods for Notification can be added here if needed
}
