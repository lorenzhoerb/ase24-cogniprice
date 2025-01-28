package ase.cogniprice.repository;

import ase.cogniprice.entity.Competitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitorRepository extends JpaRepository<Competitor, Long> {
    Optional<Competitor> findCompetitorByUrl(String competitorUrl);

    /**
     * Find competitors by name with case-insensitive matching and supports '%name%' pattern.
     *
     * @param name the name to search for in competitors' names.
     * @return a list of {@link Competitor} matching the specified name.
     */
    List<Competitor> findByNameIgnoreCaseContaining(String name);
}
