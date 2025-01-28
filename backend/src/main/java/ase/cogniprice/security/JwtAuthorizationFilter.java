package ase.cogniprice.security;

import ase.cogniprice.config.properties.SecurityProperties;
import ase.cogniprice.entity.ApiKey;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.repository.ApiKeyRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

@Service
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SecurityProperties securityProperties;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyEncoder apiKeyEncoder;


    @Autowired
    public JwtAuthorizationFilter(SecurityProperties securityProperties, ApiKeyRepository apiKeyRepository, ApiKeyEncoder apiKeyEncoder) {
        this.securityProperties = securityProperties;
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyEncoder = apiKeyEncoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        // Skip filtering for public endpoints
        String path = request.getServletPath();

        if (path.startsWith("/api/auth")) {
            chain.doFilter(request, response);  // Proceed without applying JWT filter
            return;
        }
        String authHeader = request.getHeader(securityProperties.getAuthHeader());
        LOG.debug("AuthHeader: {}", authHeader);

        try {
            if (this.validateAuthHeaderNotNull(authHeader)) {
                if (authHeader.startsWith("Bearer")) {
                    UsernamePasswordAuthenticationToken authToken = getAuthToken(request);
                    LOG.debug("authtoken: {}", authToken);
                    if (authToken != null) {
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } else if (authHeader.startsWith("apiKey")) {
                    LOG.debug("api key: {}", authHeader);
                    String rawKey = authHeader.substring("apiKey ".length());
                    UsernamePasswordAuthenticationToken apiKeyToken = validateApiKeyAndReturnUsername(rawKey);
                    SecurityContextHolder.getContext().setAuthentication(apiKeyToken);
                }
            }
        } catch (IllegalArgumentException | JwtException e) {
            LOG.debug("Invalid authorization attempt: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid authorization header or token");
            return;
        }
        chain.doFilter(request, response);

    }

    private boolean validateAuthHeaderNotNull(String authHeader) throws IllegalArgumentException {
        return authHeader != null && !authHeader.isEmpty();
    }

    private UsernamePasswordAuthenticationToken getAuthToken(HttpServletRequest request)
        throws JwtException, IllegalArgumentException {
        LOG.debug("tokeeeeeeen: {}", request);
        LOG.debug("headeeeeer: {}", securityProperties.getAuthHeader());
        String token = request.getHeader(securityProperties.getAuthHeader());
        LOG.debug("token: {}", token);
        if (token == null || token.isEmpty()) {
            return null;
        }

        LOG.debug("Auth token prefix: {}", securityProperties.getAuthTokenPrefix());
        if (!token.startsWith(securityProperties.getAuthTokenPrefix())) {
            throw new IllegalArgumentException("Authorization header is malformed or missing");
        }


        byte[] signingKey = securityProperties.getJwtSecret().getBytes();

        if (!token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token must start with 'Bearer'");
        }

        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build()
            .parseClaimsJws(token.replace(securityProperties.getAuthTokenPrefix(), ""))
            .getBody();

        LOG.debug("JWT Claims: {}", claims);

        String username = claims.getSubject();

        LOG.debug("subject: {}", username);

        List<SimpleGrantedAuthority> authorities = ((List<?>) claims
                .get("rol")).stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .toList();

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Token contains no user");
        }

        MDC.put("u", username);

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    private UsernamePasswordAuthenticationToken validateApiKeyAndReturnUsername(String rawKey) throws IllegalArgumentException {
        String hashedKey = apiKeyEncoder.encodeApiKey(rawKey);
        LOG.debug("Validating API Key: {}", hashedKey);

        Optional<ApiKey> optionalApiKey = apiKeyRepository.findByAccessKey(hashedKey);
        if (optionalApiKey.isPresent() && !optionalApiKey.get().isExpired()) {
            ApplicationUser user = optionalApiKey.get().getApplicationUser();
            return new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString())));
        } else {
            throw new IllegalArgumentException("Invalid or expired API Key");
        }
    }
}
