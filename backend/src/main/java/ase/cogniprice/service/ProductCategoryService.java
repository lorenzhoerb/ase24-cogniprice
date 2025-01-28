package ase.cogniprice.service;

import ase.cogniprice.controller.dto.product.category.ProductCategoryDto;

import java.util.List;

public interface ProductCategoryService {

    /**
     * Retrieves all product categories.
     *
     * @return a list of {@link ProductCategoryDto} representing all product categories.
     */
    List<ProductCategoryDto> getAllProductCategories();

    /**
     * Retrieves competitors filtered by name.
     *
     * @param name the name of the product categories to search for.
     * @return a list of {@link ProductCategoryDto} for product categories matching the specified name.
     */
    List<ProductCategoryDto> getProductCategoriesByName(String name);

}
