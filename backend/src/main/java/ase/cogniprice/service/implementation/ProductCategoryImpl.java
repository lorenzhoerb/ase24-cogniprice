package ase.cogniprice.service.implementation;

import ase.cogniprice.controller.dto.product.category.ProductCategoryDto;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.service.ProductCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class ProductCategoryImpl implements ProductCategoryService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProductCategoryRepository productCategoryRepository;

    @Autowired
    public ProductCategoryImpl(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }


    @Override
    public List<ProductCategoryDto> getAllProductCategories() {
        LOG.trace("getAllProductCategories()");
        return productCategoryRepository.findAll()
                .stream()
                .map(this::toProductCategoryDto)
                .toList();
    }

    @Override
    public List<ProductCategoryDto> getProductCategoriesByName(String name) {
        LOG.trace("getProductCategoriesByName({})", name);
        return productCategoryRepository.findByCategoryIgnoreCaseContaining(name)
                .stream()
                .map(this::toProductCategoryDto)
                .toList();
    }

    private ProductCategoryDto toProductCategoryDto(ProductCategory productCategory) {
        return new ProductCategoryDto(
                productCategory.getCategory()
        );
    }
}
