package ase.cogniprice.repository;

import ase.cogniprice.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    // Additional custom query methods can be added here if needed
    Optional<ApiKey> findByAccessKey(String accessKey);
}