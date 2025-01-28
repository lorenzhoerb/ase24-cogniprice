package ase.cogniprice.controller.mapper;

import ase.cogniprice.controller.dto.product.ProductBatchImporterDto;
import ase.cogniprice.controller.dto.product.ProductCreateDto;
import ase.cogniprice.controller.dto.product.ProductDetailsDto;
import ase.cogniprice.controller.dto.product.ProductUpdateDto;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.ProductImage;
import ase.cogniprice.repository.ProductCategoryRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productCategory", expression = "java(stringToProductCategory(productCreateDto.getProductCategoryId(), productCategoryRepository))")
    Product toEntityWithoutImage(ProductCreateDto productCreateDto, ProductCategoryRepository productCategoryRepository);

    @Mapping(target = "productCategory", expression = "java(stringToProductCategory(productBatchImporterDto.getProductCategory(), productCategoryRepository))")
    Product toEntityWithoutImage(ProductBatchImporterDto productBatchImporterDto, ProductCategoryRepository productCategoryRepository);



    @Mapping(target = "name", expression = "java(productUpdateDto.getName() == null ? product.getName() : productUpdateDto.getName())")
    @Mapping(target = "gtin", expression = "java(productUpdateDto.getGtin() == null ? product.getGtin() : productUpdateDto.getGtin())")
    @Mapping(target = "productCategory", expression = "java(productUpdateDto.getProductCategoryId() == null ? product.getProductCategory() : stringToProductCategory(productUpdateDto.getProductCategoryId(), productCategoryRepository))")
    @Mapping(target = "productImage", expression = "java(productUpdateDto.getImage() == null ? product.getProductImage() : mapProductImage(productUpdateDto.getImage(), product))")
    void updateEntity(@MappingTarget Product product, ProductUpdateDto productUpdateDto,
                      ProductCategoryRepository productCategoryRepository);

    @Mapping(target = "productImage", expression = "java(mapProductImage(image, product))")
    @Mapping(target = "name", ignore = true)
    void mapProductImageIntoEntity(@MappingTarget Product product, MultipartFile image);

    @Mapping(source = "productCategory.category", target = "category") // Map the category name
    @Mapping(target = "imageBase64", expression = "java(product.getProductImage() == null ? null : encodeImageToBase64(product.getProductImage().getThumbnail()))")
    ProductDetailsDto productToProductDetailsDto(Product product);

    default String encodeImageToBase64(byte[] image) {
        if (image == null) {
            return null;
        } else {
            return Base64.getEncoder().encodeToString(image);
        }
    }

    default ProductImage mapProductImage(MultipartFile image, Product product) {
        if (image == null || image.isEmpty()) {
            return null; // Return null if no image is provided
        }
        ProductImage productImage = new ProductImage();
        try {
            if (product.getProductImage() != null) {
                productImage.setId(product.getProductImage().getId());
            }
            productImage.setImage(image.getBytes()); // Convert to byte[]
            productImage.setContentType(image.getContentType()); // Extract content type
            productImage.setFileName(image.getOriginalFilename()); // Extract file name
            productImage.setSize(image.getSize()); // Extract file size
            productImage.setThumbnail(generateThumbnail(image));
            productImage.setProduct(product);
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file", e);
        }
        return productImage;
    }

    // Custom method to convert String to ProductCategory
    @Named("stringToProductCategory")
    default ProductCategory stringToProductCategory(String categoryName, ProductCategoryRepository productCategoryRepository) {
        if (categoryName == null || categoryName.isEmpty()) {
            return null;
        }
        // Fetch the ProductCategory from the repository
        return productCategoryRepository.findById(categoryName)
            .orElseThrow(() -> new IllegalArgumentException("Product category not found with name: " + categoryName));
    }

    // Generate a thumbnail for the image
    private byte[] generateThumbnail(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Invalid image file: file is null or empty");
        }

        // Validate content type
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid image file: unsupported content type " + contentType);
        }

        ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();

        try {
            // Create the thumbnail
            Thumbnails.of(image.getInputStream())
                    .size(225, 225) // Thumbnail size
                    .outputFormat("png") // Output format
                    .toOutputStream(thumbnailOutputStream);

        } catch (IOException e) {
            // Log and rethrow the exception for better debugging
            throw new IOException("Error generating thumbnail: " + e.getMessage(), e);
        }

        return thumbnailOutputStream.toByteArray();
    }

}
