package ase.cogniprice.security;

import ase.cogniprice.config.properties.SecurityProperties;
import ase.cogniprice.type.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtTokenizer {

    private final SecurityProperties securityProperties;

    public JwtTokenizer(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String getAuthToken(String user, Long userId, List<String> roles) {
        byte[] signingKey = securityProperties.getJwtSecret().getBytes();
        String token = Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
            .setHeaderParam("typ", securityProperties.getJwtType())
            .setIssuer(securityProperties.getJwtIssuer())
            .setAudience(securityProperties.getJwtAudience())
            .setSubject(user)
            .setExpiration(new Date(System.currentTimeMillis() + securityProperties.getJwtExpirationTime()))
            .claim("rol", roles)
            .claim("user_id", userId)
            .compact();
        return securityProperties.getAuthTokenPrefix() + " " + token;
    }

    // If using this, change authorities to work with list
    public String getAuthToken(String user, List<String> roles) {
        byte[] signingKey = securityProperties.getJwtSecret().getBytes();
        String token = Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
            .setHeaderParam("typ", securityProperties.getJwtType())
            .setIssuer(securityProperties.getJwtIssuer())
            .setAudience(securityProperties.getJwtAudience())
            .setSubject(user)
            .setExpiration(new Date(System.currentTimeMillis() + securityProperties.getJwtExpirationTime()))
            .claim("rol", roles)
            .compact();
        return securityProperties.getAuthTokenPrefix() + " " + token;
    }

}
