package ase.cogniprice.unitTest.timescaleDB;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimescaleDbHypertableTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    public void init() {
        // Create the hypertable with price_time as the partitioning column
        jdbcTemplate.execute("SELECT create_hypertable('product_price', 'price_time', if_not_exists => true);");
    }

    @Test
    void testIfProductPriceIsHypertable() {
        // Query to check if the table is a hypertable
        String checkHypertableQuery = "SELECT count(*) FROM timescaledb_information.hypertables WHERE hypertable_name = 'product_price'";

        // Execute the query and get the result
        Integer count = jdbcTemplate.queryForObject(checkHypertableQuery, Integer.class);

        // Assert that the table exists in hypertables (i.e., count should be > 0)
        assertTrue(count > 0, "The product_price table should be a hypertable.");
    }

}
