package ase.cogniprice.config;

import ase.cogniprice.config.properties.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtTestUtils {

    private final SecurityProperties securityProperties;

    @Autowired
    public JwtTestUtils(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    // Method to generate a valid JWT token with the given username and roles
    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
            .setSubject(username)
            .claim("rol", roles)  // Claims with roles (your filter expects "rol" claim)
            .signWith(SignatureAlgorithm.HS256, securityProperties.getJwtSecret().getBytes()) // Sign with secret
            .compact();
    }
}
