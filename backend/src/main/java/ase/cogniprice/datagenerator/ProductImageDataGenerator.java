package ase.cogniprice.datagenerator;

import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductImage;
import ase.cogniprice.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.transaction.annotation.Transactional;

@Profile("generateData")
@Component
@DependsOn({"productDataGenerator"})
public class ProductImageDataGenerator {

    private final ProductRepository productRepository;

    public ProductImageDataGenerator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    @PostConstruct
    public void generateProducts() throws IOException {
        Map<String, ProductImage> productImageMap = this.loadImages();

        for (Product product : this.productRepository.findAll()) {
            ProductImage matchedImage = productImageMap.get(product.getName());
            if (matchedImage != null) {
                product.setProductImage(matchedImage);
                matchedImage.setProduct(product);
                this.productRepository.save(product);
            }
        }
    }

    private Map<String, ProductImage> loadImages() throws IOException {
        Map<String, ProductImage> productImageMap = new HashMap<>();

        // Use a HashMap for large mappings
        Map<String, String> imageMappings = new HashMap<>();
        imageMappings.put("iPhone 15", "iphone.jpeg");
        imageMappings.put("Samsung Galaxy S23 Rosa", "samsung_phone.jpg");
        imageMappings.put("Dyson V11 Vacuum", "dyson.jpeg");
        imageMappings.put("Levi's 501 Original Jeans", "Jeans.jpg");
        imageMappings.put("L'Oréal Revitalift Cream", "loreal.jpg");
        imageMappings.put("Atomic Habits Book", "atomic_habits.jpeg");
        imageMappings.put("Cube Mountain Bike", "cube_mountainbike.jpeg");
        imageMappings.put("LEGO® Star Wars 10188 Todesstern", "lego_star_wars.png");
        imageMappings.put("Starbucks Dark Roast Coffee", "starbucks.jpg");
        imageMappings.put("Apple iPhone 14 Pro Max 128GB", "iphone.jpeg");
        imageMappings.put("Samsung QLED 4K Smart TV 65-Inch", "samsung_tv.jpeg");
        imageMappings.put("Dyson V11 Absolute Cordless Vacuum Cleaner", "dyson.jpeg");
        imageMappings.put("The Catcher in the Rye by J.D. Salinger", "the_adventure_challenge.jpeg");
        imageMappings.put("Nike Air Zoom Pegasus 39", "nike_air.jpeg");
        imageMappings.put("LEGO Star Wars Millennium Falcon Set", "lego_star_wars.png");
        imageMappings.put("Fitbit Charge 5 Fitness Tracker", "fitbit.jpg");
        imageMappings.put("Omega Seamaster 300M Diver Watch", "omega.jpeg");
        imageMappings.put("Now Essential Oils Lavender 1oz", "essential_oils.jpg");
        imageMappings.put("Call of Duty: Modern Warfare II", "callofduty.jpeg");
        imageMappings.put("GoPro HERO12 Black Action Camera", "wireless_headphones.jpg");
        imageMappings.put("The Adventure Challenge: Couples Edition", "the_adventure_challenge.jpeg");
        imageMappings.put("Hape Wooden Art Easel for Kids", "lego_star_wars.png");
        imageMappings.put("Pelican Elite 20QT Cooler", "pelican-americana-collection-cooler-20qt.jpg");
        imageMappings.put("Whiskas Dry Cat Food Tuna Flavor 3kg", "whiskas.jpeg");

        Path imageDirectory = Paths.get("src/main/resources/datageneratorImages");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(imageDirectory, "*.{jpg,jpeg,png}")) {
            for (Path filePath : stream) {
                String fileName = filePath.getFileName().toString();

                if (imageMappings.containsValue(fileName)) {
                    ProductImage productImage = new ProductImage();

                    // Set the image data
                    productImage.setImage(Files.readAllBytes(filePath));
                    productImage.setFileName(fileName);
                    productImage.setContentType(Files.probeContentType(filePath));
                    productImage.setSize(Files.size(filePath));
                    productImage.setThumbnail(generateThumbnail(filePath));

                    // Match the image with its product name
                    imageMappings.forEach((productName, imageName) -> {
                        if (fileName.equals(imageName)) {
                            productImageMap.put(productName, productImage);
                        }
                    });
                }
            }
        }
        return productImageMap;
    }

    private byte[] generateThumbnail(Path filePath) throws IOException {
        try (ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(filePath.toFile())
                .size(225, 225)
                .outputFormat("png")
                .toOutputStream(thumbnailOutputStream);

            return thumbnailOutputStream.toByteArray();
        }
    }
}
