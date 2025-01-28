package ase.cogniprice.config.properties;

import ase.cogniprice.config.SecurityPropertiesConfig;
import org.springframework.stereotype.Component;

/**
 * This configuration class offers all necessary security properties in an immutable manner.
 */
@Component
public class SecurityProperties {

    private final SecurityPropertiesConfig.Auth auth;
    private final SecurityPropertiesConfig.Jwt jwt;
    private final SecurityPropertiesConfig.ApiKey apiKey;

    public SecurityProperties(SecurityPropertiesConfig.Auth auth,
                              SecurityPropertiesConfig.Jwt jwt,
                              SecurityPropertiesConfig.ApiKey apiKey
    ) {
        this.auth = auth;
        this.jwt = jwt;
        this.apiKey = apiKey;
    }

    public String getAuthHeader() {
        return auth.getHeader();
    }

    public String getAuthTokenPrefix() {
        return auth.getPrefix();
    }

    public String getLoginUri() {
        return auth.getLoginUri();
    }

    public String getJwtSecret() {
        return jwt.getSecret();
    }

    public String getJwtType() {
        return jwt.getType();
    }

    public String getJwtIssuer() {
        return jwt.getIssuer();
    }

    public String getJwtAudience() {
        return jwt.getAudience();
    }

    public Long getJwtExpirationTime() {
        return jwt.getExpirationTime();
    }

    public String getApiKeySecret() {
        return apiKey.getSecret();
    }

}
