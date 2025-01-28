package ase.cogniprice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "scheduler")
@ConfigurationPropertiesScan
@Data
public class SchedulerConfigProperties {
    private Integer maxRetryAttempts;
    private Integer jobDispatchLimit;
}
