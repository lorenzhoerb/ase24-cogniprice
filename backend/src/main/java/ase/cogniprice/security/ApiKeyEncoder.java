package ase.cogniprice.security;

import ase.cogniprice.config.properties.SecurityProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class ApiKeyEncoder {

    private final SecurityProperties securityProperties;

    public ApiKeyEncoder(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String encodeApiKey(String rawApiKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(securityProperties.getApiKeySecret().getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(rawApiKey.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode API Key ", e);
        }
    }
}
