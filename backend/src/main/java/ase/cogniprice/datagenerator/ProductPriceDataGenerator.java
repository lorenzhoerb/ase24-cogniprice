package ase.cogniprice.datagenerator;

import ase.cogniprice.entity.ProductPrice;
import ase.cogniprice.entity.ProductPrice.ProductPriceId;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ProductPriceRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import jakarta.annotation.PostConstruct;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Profile({"generateData", "generateDemoData"})
@Component
@DependsOn({"applicationUserDataGenerator",
    "productCategoryDataGenerator",
    "competitorDataGenerator",
    "timescaleHypertableInitializer",
    "storeProductDataGenerator"})
public class ProductPriceDataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ProductPriceDataGenerator.class);

    private final ProductPriceRepository productPriceRepository;
    private final StoreProductRepository storeProductRepository;
    private final Environment environment;


    @Autowired
    public ProductPriceDataGenerator(ProductPriceRepository productPriceRepository,
                                     StoreProductRepository storeProductRepository, Environment environment, ProductRepository productRepository, CompetitorRepository competitorRepository) {
        this.productPriceRepository = productPriceRepository;
        this.storeProductRepository = storeProductRepository;
        this.environment = environment;
    }

    @PostConstruct
    public void generateProductPrices() {
        List<String> activeProfiles = Arrays.stream(environment.getActiveProfiles()).toList();
        List<StoreProduct> storeProducts = storeProductRepository.findAll();

        if (activeProfiles.contains("generateDemoData")) {
            for (StoreProduct product : storeProducts) {
                this.generateDemoProductPrices(product);
            }
        } else {
            LocalDateTime startTime = LocalDateTime.now().minusDays(30); // Start date for price data (5 days ago)
            Random random = new Random();

            for (StoreProduct product : storeProducts) {
                generateTimedPricesForProductCompetitor(product, startTime, random);
            }
        }
    }

    private void generateDemoProductPrices(StoreProduct storeProduct) {
        int days = 1100;
        String competitor = storeProduct.getCompetitor().getName();
        String filePath = "src/main/resources/datagenaratorPrices/" + competitor + "/DemoProduct1.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);

            String line;
            for (int i = 0; i < days; i++) {
                LocalDateTime timestamp = startTime.plusDays(i);
                ProductPriceId productPriceId = new ProductPriceId(storeProduct.getId(), timestamp);

                // Read one line from the file
                if ((line = reader.readLine()) != null) {
                    // Process the line here
                    Money price =  Money.of(Float.parseFloat(line), "EUR");

                    ProductPrice productPrice = new ProductPrice();
                    productPrice.setId(productPriceId);
                    productPrice.setStoreProduct(storeProduct);
                    productPrice.setPrice(price);

                    productPriceRepository.save(productPrice);
                    LOG.info("Saved price for storeProduct: {} at time: {} with price: {}",
                        productPrice.getId().getStoreProductId(), timestamp, price);
                } else {
                    LOG.warn("File ended before completing all days. Day {} has no data.", i);
                    break;
                }
            }

        } catch (IOException e) {
            LOG.error("Error reading from file {}: {}", filePath, e.getMessage());
        }
    }


    private void generateTimedPricesForProductCompetitor(StoreProduct storeProduct, LocalDateTime startTime, Random random) {
        int days = 30;
        for (int i = 0; i < days; i++) {
            LocalDateTime timestamp = startTime.plusDays(i);
            Money price = generateRandomPrice(random);
            ProductPriceId productPriceId = new ProductPriceId(storeProduct.getId(), timestamp);

            // Check if the ProductPrice entry already exists
            if (!productPriceRepository.existsById(productPriceId)) {
                ProductPrice productPrice = new ProductPrice();
                productPrice.setId(productPriceId);
                productPrice.setStoreProduct(storeProduct);
                productPrice.setPrice(price);

                productPriceRepository.save(productPrice);
                LOG.info("Saved price for storeProduct: {} at time: {} with price: {}",
                        productPrice.getId().getStoreProductId(), timestamp, price);
            }
        }
    }

    private Money generateRandomPrice(Random random) {
        // Generate a random price between a base range for demonstration (e.g., 50 - 500)
        return Money.of(50 + (random.nextDouble() * (500 - 50)), "EUR");
    }
}
