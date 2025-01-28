package ase.cogniprice.repository;

import ase.cogniprice.entity.ApplicationUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
    @EntityGraph(attributePaths = {"id", "username", "password", "firstName", "lastName", "email", "loginAttempts", "userLocked", "role"})
    ApplicationUser findApplicationUserByEmail(String email);

    @EntityGraph(attributePaths = {"id", "username", "password", "firstName", "lastName", "email", "loginAttempts", "userLocked", "role"})
    ApplicationUser findApplicationUserByUsername(String username);

    @EntityGraph(attributePaths = {
        "id", "username", "password", "firstName", "lastName", "email", "loginAttempts", "userLocked", "role",
        "notifications", "apiKey", "storeProducts", "productUserPricingRules"
    })
    ApplicationUser findApplicationUserWithRelationsByUsername(String username);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ApplicationUser u SET u.userLocked = :locked WHERE u.id = :id")
    int updateUserLockedStatus(@Param("id") Long id, @Param("locked") boolean locked);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ApplicationUser u SET u.loginAttempts = :loginAttempts WHERE u.id = :id")
    void updateUserLoginAttempts(Long id, Long loginAttempts);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ApplicationUser u SET u.loginAttempts = u.loginAttempts + 1 WHERE u.id = :id")
    int incrementLoginAttempts(@Param("id") Long id);

    @Transactional
    void deleteById(Long id);

    @Transactional
    void deleteByUsername(String username);

    @Transactional
    @Modifying
    @Query(value = """
        DELETE FROM user_store_product
        WHERE application_user_id = (
            SELECT id FROM application_user WHERE username = :username
        )
        AND product_id = :productId
        """, nativeQuery = true)
    int deleteProductFromWatchlist(@Param("username") String username, @Param("productId") Long productId);
}
