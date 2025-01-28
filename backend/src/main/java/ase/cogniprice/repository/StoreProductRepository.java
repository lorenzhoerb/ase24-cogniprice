package ase.cogniprice.repository;

import ase.cogniprice.controller.dto.product.ProductDetailsDto;
import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.controller.dto.store.product.StoreProductWithPriceDto;
import ase.cogniprice.controller.dto.store.product.StoreProductDetailsDto;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.StoreProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreProductRepository extends JpaRepository<StoreProduct, StoreProduct.StoreProductId> {
    @Query("SELECT sp FROM StoreProduct sp " +
            "WHERE sp.crawlState = ase.cogniprice.type.CrawlState.SCHEDULED " +
            "AND sp.nextCrawl <= CURRENT_TIMESTAMP " +
            "ORDER BY sp.nextCrawl ASC")
    List<StoreProduct> findScheduledJobs();

    @Query("SELECT sp FROM StoreProduct sp " +
            "WHERE sp.crawlState = ase.cogniprice.type.CrawlState.SCHEDULED " +
            "AND sp.nextCrawl <= CURRENT_TIMESTAMP " +
            "ORDER BY sp.nextCrawl ASC " +
            "LIMIT :limit ")
    List<StoreProduct> findScheduledJobs(@Param("limit") int limit);

    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN TRUE ELSE FALSE END " +
        "FROM ApplicationUser u JOIN u.storeProducts sp " +
        "WHERE u.username = :username AND sp.id = :storeProductId")
    boolean existsStoreProductForUser(@Param("username") String username, @Param("storeProductId") StoreProduct.StoreProductId storeProductId);

    @Query("""
        SELECT new ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto(
            sp.id.productId,
            sp.product.name,
            sp.product.gtin,
            pi.thumbnail,
            sp.product.productImageUrl,
            sp.product.productCategory.category)
        FROM StoreProduct sp
        LEFT JOIN ProductImage pi ON sp.product.id = pi.product.id
        JOIN sp.applicationUsers au
        WHERE au.username = :username
        AND LOWER(sp.product.name) LIKE LOWER(CONCAT('%', :name, '%'))
        AND LOWER(sp.product.productCategory.category) LIKE LOWER(CONCAT('%', :category, '%'))
        GROUP BY  sp.id.productId, sp.product.name,  pi.thumbnail, sp.product.productImageUrl, sp.product.productCategory.category, sp.product.gtin
        ORDER BY sp.id.productId
        """)
    Page<ProductDetailsWithPricesDto> findStoreProductsWithImageByUser(@Param("username") String username, @Param("name") String name, @Param("category") String category, Pageable pageable);

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
                c.name,
                pp.price_time,
                pp.price,
                pp.currency
            FROM product_price pp
            join competitor c on c.id = pp.competitor_id
            JOIN LatestEntryDates led ON\s
                pp.product_id = led.product_id AND\s
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
            pld.name,
            pld.price_time,
            pld.price,
            pld.currency,
            pld.competitor_id,
            CASE
                WHEN pld.price = mmp.max_price THEN 'Max Price'
                WHEN pld.price = mmp.min_price THEN 'Min Price'
            END AS price_type
        FROM PricesOnLatestDate pld
        JOIN MaxMinPrices mmp ON pld.product_id = mmp.product_id
        WHERE pld.price = mmp.max_price OR pld.price = mmp.min_price;
        """, nativeQuery = true)
    List<Object[]> getProductPriceMinAndMax();

    List<StoreProduct> findStoreProductByProductId(Long productId);
}
