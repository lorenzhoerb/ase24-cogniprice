package ase.cogniprice.datagenerator;


import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ProductCategoryRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Profile({"generateData", "generateCategories", "generateDemoData"})
@Component
public class ProductCategoryDataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProductCategoryRepository productCategoryRepository;

    @Autowired
    public ProductCategoryDataGenerator(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    @PostConstruct
    private void generateProductCategories() {
        //List<String> categories = loadCategoriesFromFile("product-categories.txt");
        List<String> categories = getCategories();

        for (String category : categories) {
            if (!productCategoryRepository.existsById(category)) {
                ProductCategory productCategory = new ProductCategory();
                productCategory.setCategory(category);
                productCategoryRepository.save(productCategory);
                LOG.info("Created product category: {}", category);
            }
        }
    }

    private List<String> loadCategoriesFromFile(String fileName) {
        List<String> categories = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                categories.add(line.trim());
            }
        } catch (Exception e) {
            LOG.error("Error loading product categories from file: {}", fileName, e);
        }
        return categories;
    }

    private List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Electronics");
        categories.add("Home Appliances");
        categories.add("Fashion");
        categories.add("Personal Care");
        categories.add("Books");
        categories.add("Sports");
        categories.add("Toys & Games");
        categories.add("Groceries");
        categories.add("Furniture");
        categories.add("Automotive");
        categories.add("Health & Wellness");
        categories.add("Office Supplies");
        categories.add("Garden & Outdoor");
        categories.add("Pet Supplies");
        categories.add("Music & Movies");
        categories.add("Jewelry");
        categories.add("Baby Products");
        categories.add("Tools & Hardware");
        categories.add("Art & Craft Supplies");
        categories.add("Stationery");
        categories.add("Footwear");
        categories.add("Mobile Phones & Accessories");
        categories.add("Cameras & Photography");
        categories.add("Video Games");
        categories.add("Watches");
        categories.add("Food & Beverages");
        categories.add("Kitchenware");
        categories.add("Luggage & Travel Gear");
        categories.add("Cleaning Supplies");
        categories.add("Seasonal Décor");
        categories.add("Party Supplies");
        categories.add("Industrial Equipment");
        categories.add("Collectibles");
        categories.add("Luxury Goods");
        categories.add("Energy & Lighting");
        categories.add("Outdoor Adventure Gear");
        categories.add("Fitness Equipment");
        categories.add("Medical Supplies");
        categories.add("Software");
        categories.add("Educational Supplies");
        categories.add("Antiques");
        categories.add("Gift Cards & Vouchers");
        categories.add("Building Materials");
        categories.add("Scientific Instruments");
        return categories;
    }

}
