package ase.cogniprice.config;

import ase.cogniprice.entity.ApiKey;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityPropertiesConfig {

    @Bean
    @ConfigurationProperties(prefix = "security.auth")
    protected Auth auth() {
        return new Auth();
    }

    @Bean
    @ConfigurationProperties(prefix = "security.jwt")
    protected Jwt jwt() {
        return new Jwt();
    }

    @Bean
    @ConfigurationProperties(prefix = "security.apikey")
    protected ApiKey apiKey() {
        return new ApiKey();
    }

    @Getter
    @Setter
    public static class Auth {
        private String header;
        private String prefix;
        private String loginUri;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private String type;
        private String issuer;
        private String audience;
        private Long expirationTime;
    }

    @Getter
    @Setter
    public static class ApiKey {
        private String secret;
    }
}
