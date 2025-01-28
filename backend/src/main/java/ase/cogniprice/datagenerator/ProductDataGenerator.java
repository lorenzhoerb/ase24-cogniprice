package ase.cogniprice.datagenerator;

import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

@Profile({"generateData", "generateDemoData"})
@Component
@DependsOn({"applicationUserDataGenerator", "productCategoryDataGenerator", "competitorDataGenerator"})
public class ProductDataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final Environment environment;


    @Autowired
    public ProductDataGenerator(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, Environment environment) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.environment = environment;
    }

    @PostConstruct
    private void generateProducts() {
        List<String> activeProfiles = Arrays.stream(environment.getActiveProfiles()).toList();
        List<Product> products;
        if (activeProfiles.contains("generateDemoData")) {
            products = Arrays.asList(
                createProduct("3453262652", "DemoProduct1", "Electronics")
                );
        } else {
            // Specific products with category names and GTINs
            products = Arrays.asList(
                createProduct("0195949037863", "iPhone 15", "Electronics"),
                createProduct("8806094724905", "Samsung Galaxy S23 Rosa", "Electronics"),
                createProduct("5025155082003", "Dyson V11 Vacuum", "Home Appliances"),
                createProduct("052177002264", "Levi's 501 Original Jeans", "Fashion"),
                createProduct("071249104583", "L'Oréal Revitalift Cream", "Personal Care"),
                createProduct("743399736005", "Atomic Habits Book", "Books"),
                createProduct("4054571386366", "Cube Mountain Bike", "Sports"),
                createProduct("0673419130578", "LEGO® Star Wars 10188 Todesstern", "Toys & Games"),
                createProduct("762111206022", "Starbucks Dark Roast Coffee", "Groceries"),
                createProduct("8901101104263", "Apple iPhone 14 Pro Max 128GB", "Mobile Phones & Accessories"),
                createProduct("4013496962548", "Samsung QLED 4K Smart TV 65-Inch", "Electronics"),
                createProduct("075900251700", "Dyson V11 Absolute Cordless Vacuum Cleaner", "Home Appliances"),
                createProduct("9780316769488", "The Catcher in the Rye by J.D. Salinger", "Books"),
                createProduct("081098490807", "Nike Air Zoom Pegasus 39", "Footwear"),
                createProduct("081000483295", "LEGO Star Wars Millennium Falcon Set", "Toys & Games"),
                createProduct("022255120509", "Fitbit Charge 5 Fitness Tracker", "Fitness Equipment"),
                createProduct("016102324035", "Omega Seamaster 300M Diver Watch", "Luxury Goods"),
                createProduct("073373902142", "Now Essential Oils Lavender 1oz", "Health & Wellness"),
                createProduct("023942001567", "Call of Duty: Modern Warfare II", "Video Games"),
                createProduct("020548151020", "GoPro HERO12 Black Action Camera", "Cameras & Photography"),
                createProduct("9781449496364", "The Adventure Challenge: Couples Edition", "Gift Cards & Vouchers"),
                createProduct("081433804121", "Hape Wooden Art Easel for Kids", "Art & Craft Supplies"),
                createProduct("019322372102", "Pelican Elite 20QT Cooler", "Outdoor Adventure Gear"),
                createProduct("084334210052", "Whiskas Dry Cat Food Tuna Flavor 3kg", "Pet Supplies")
            );
        }

        for (Product product : products) {
            if (productRepository.findProductByGtin(product.getGtin()) == null) {
                productRepository.save(product);
                LOG.info("Created product: {} with GTIN: {}", product.getName(), product.getGtin());
            }
        }
    }

    private Product createProduct(String gtin, String name, String categoryName) {
        Product product = new Product();
        product.setGtin(gtin);
        product.setName(name);

        // Set the category if it exists
        ProductCategory category = productCategoryRepository.findById(categoryName).orElse(null);
        if (category != null) {
            // Assuming Product has a setCategory method and a relationship to ProductCategory
            product.setProductCategory(category);
        }
        return product;
    }
}

