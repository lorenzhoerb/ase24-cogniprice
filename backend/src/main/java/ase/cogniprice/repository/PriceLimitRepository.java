package ase.cogniprice.repository;

import ase.cogniprice.entity.PriceLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceLimitRepository extends JpaRepository<PriceLimit, Long> {
}
