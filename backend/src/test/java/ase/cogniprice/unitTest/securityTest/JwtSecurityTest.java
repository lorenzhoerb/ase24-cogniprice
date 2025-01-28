package ase.cogniprice.unitTest.securityTest;

import ase.cogniprice.config.properties.SecurityProperties;
import ase.cogniprice.security.JwtTokenizer;
import ase.cogniprice.type.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JwtSecurityTest {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Test
    void testCreateJwt() {
        // Given
        String username = "test_user";
        Long id = 1L;
        String signingKey = securityProperties.getJwtSecret();
        Role role = Role.USER;

        // When
        String token = jwtTokenizer.getAuthToken(username, id, List.of(role.name()));
        // Remove the "Bearer " prefix before parsing the token
        String jwtToken = token.replace(securityProperties.getAuthTokenPrefix(), "");

        // Then
        assertNotNull(jwtToken, "Token should not be null");

        Claims claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(signingKey.getBytes()))  // Convert the signing key to bytes
            .build()
            .parseClaimsJws(jwtToken)
            .getBody();

        assertEquals(username, claims.getSubject(), "JWT subject should be the username");
        assertEquals(id.intValue(), claims.get("user_id"), "JWT id should be user id");
        Object roleClaim = claims.get("rol");
        assertNotNull(roleClaim, "Role claim should not be null");
        List<String> roles = (List<String>) roleClaim;
        assertTrue(roles.contains(role.name()), "JWT role should contain the expected role");
        assertNotNull(claims.getExpiration(), "JWT should have an expiration date");
    }
}
