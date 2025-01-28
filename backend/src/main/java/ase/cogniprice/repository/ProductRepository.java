package ase.cogniprice.repository;

import ase.cogniprice.controller.dto.product.ProductDetailsWithCompetitorUrlDto;
import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    Product findProductByGtin(String gtin);

    @Query("""

            SELECT sp.product
        FROM ApplicationUser au
        JOIN au.storeProducts sp
        WHERE au.username = :username
        """)
    List<Product> findProductsByUser(@Param("username") String username);

    @Query("""

            SELECT sp.product
        FROM ApplicationUser au
        JOIN au.storeProducts sp
        WHERE au.username = :username
        AND sp.product.id = :productId
        """)
    Optional<Product> findProductOfUserById(@Param("username") String username, @Param("productId") Long productId);

    @Query("""
        SELECT p
        FROM Product p
        WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(p.gtin) LIKE LOWER(CONCAT('%', :query, '%')))
        AND LOWER(p.productCategory.category) LIKE LOWER(CONCAT('%', :category, '%'))
        """)
    Page<Product> getFilteredProducts(String query, String category, Pageable pageable);

    @Query("""

        SELECT new ase.cogniprice.controller.dto.product.ProductDetailsWithCompetitorUrlDto(
            p.id,
            p.name,
            p.gtin,
            pi.image,
            p.productImageUrl,
            p.productCategory.category,
            sp.productUrl
        )
        FROM StoreProduct sp
        JOIN sp.product p
        LEFT JOIN ProductImage pi ON pi.product.id = p.id
        WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(p.gtin) LIKE LOWER(CONCAT('%', :query, '%')))
        AND LOWER(p.productCategory.category) LIKE LOWER(CONCAT('%', :category, '%'))
        AND sp.competitor.id = :competitorId
        """)
    Page<ProductDetailsWithCompetitorUrlDto> getFilteredProductsByCompetitorId(String query, String category, Pageable pageable, Long competitorId);


    @Query("""
        SELECT new ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto(
            p.id,
            p.name,
            p.gtin,
            pi.image,
            p.productImageUrl,
            p.productCategory.category,
            null,
            null
        )
        FROM Product p
        LEFT JOIN ProductImage pi ON pi.product.id = p.id
        LEFT JOIN p.productCategory pc
        WHERE p.id = :productId
        """)
    ProductDetailsWithPricesDto findProductDetailsById(@Param("productId") Long productId);

    @Query(value = """
        WITH LatestEntryDates AS (
            SELECT
                product_id,
                MAX(CAST(price_time AS DATE)) AS latest_date
            FROM product_price
            GROUP BY product_id
        ),
        PricesOnLatestDate AS (
            SELECT
                pp.product_id,
                pp.competitor_id,
                c.name AS competitor_name,
                pp.price_time,
                pp.price,
                pp.currency
            FROM product_price pp
            JOIN competitor c ON c.id = pp.competitor_id
            JOIN LatestEntryDates led ON
                pp.product_id = led.product_id AND
                CAST(pp.price_time AS DATE) = led.latest_date
        ),
        MaxMinPrices AS (
            SELECT
                product_id,
                MAX(price) AS max_price,
                MIN(price) AS min_price
            FROM PricesOnLatestDate
            GROUP BY product_id
        )
        SELECT
            pld.product_id,
            pld.competitor_id,
            pld.competitor_name,
            pld.price_time,
            pld.price,
            pld.currency,
            CASE
                WHEN pld.price = mmp.max_price THEN 'Max Price'
                WHEN pld.price = mmp.min_price THEN 'Min Price'
            END AS price_type
        FROM PricesOnLatestDate pld
        JOIN MaxMinPrices mmp ON pld.product_id = mmp.product_id
        WHERE pld.product_id = :productId
          AND (pld.price = mmp.max_price OR pld.price = mmp.min_price)
        """, nativeQuery = true)
    List<Object[]> findMinMaxPricesByProductId(@Param("productId") Long productId);
}
