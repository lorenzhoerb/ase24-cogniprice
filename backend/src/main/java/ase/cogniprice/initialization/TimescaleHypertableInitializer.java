package ase.cogniprice.initialization;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TimescaleHypertableInitializer {

    private final JdbcTemplate jdbcTemplate;

    public TimescaleHypertableInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        // Create the hypertable with price_time as the partitioning column
        jdbcTemplate.execute("SELECT create_hypertable('product_price', 'price_time', if_not_exists => true);");
    }
}
