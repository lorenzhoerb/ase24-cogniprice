package ase.cogniprice.repository;

import ase.cogniprice.controller.dto.store.product.PriceDto;
import ase.cogniprice.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, ProductPrice.ProductPriceId> {

    @Query("""
        SELECT new ase.cogniprice.controller.dto.store.product.PriceDto(
            sp.competitor.id,
            pp.price,
            sp.competitor.name,
            pp.currency,
            pp.id.priceTime
        )
        FROM ProductPrice pp
        JOIN pp.storeProduct sp
        WHERE sp.id.productId = :productId
        ORDER BY sp.competitor.name, pp.id.priceTime
        """)
    List<PriceDto> findProductPricesByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT MIN(pp.price)
            FROM ProductPrice pp
            WHERE pp.id.storeProductId.productId = :productId
              AND pp.id.priceTime = (
                  SELECT MAX(pp2.id.priceTime)
                  FROM ProductPrice pp2
                  WHERE pp2.id.storeProductId.productId = pp.id.storeProductId.productId
                    AND pp2.id.storeProductId.competitorId = pp.id.storeProductId.competitorId
              )
            GROUP BY pp.id.storeProductId.productId
            """)
    Optional<BigDecimal> findMinPriceByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT MAX(pp.price)
            FROM ProductPrice pp
            WHERE pp.id.storeProductId.productId = :productId
              AND pp.id.priceTime = (
                  SELECT MAX(pp2.id.priceTime)
                  FROM ProductPrice pp2
                  WHERE pp2.id.storeProductId.productId = pp.id.storeProductId.productId
                    AND pp2.id.storeProductId.competitorId = pp.id.storeProductId.competitorId
              )
            GROUP BY pp.id.storeProductId.productId
            """)
    Optional<BigDecimal> findMaxPriceByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT AVG(pp.price)
            FROM ProductPrice pp
            WHERE pp.id.storeProductId.productId = :productId
              AND pp.id.priceTime = (
                  SELECT MAX(pp2.id.priceTime)
                  FROM ProductPrice pp2
                  WHERE pp2.id.storeProductId.productId = pp.id.storeProductId.productId
                    AND pp2.id.storeProductId.competitorId = pp.id.storeProductId.competitorId
              )
            GROUP BY pp.id.storeProductId.productId
            """)
    Optional<BigDecimal> findAvgPriceByProductId(@Param("productId") Long productId);
}
