package ase.cogniprice.repository;

import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, String> {
    /**
     * Find product categories by name with case-insensitive matching and supports '%name%' pattern.
     *
     * @param name the name to search for in product categories.
     * @return a list of {@link ProductCategory} matching the specified name.
     */
    List<ProductCategory> findByCategoryIgnoreCaseContaining(String name);
}
