package ase.cogniprice.controller.mapper;

import ase.cogniprice.controller.dto.store.product.StoreProductCreateDto;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface StoreProductMapper {

    @Mapping(target = "id.productId", source = "productId")
    @Mapping(target = "id.competitorId", source = "competitorId")
    @Mapping(target = "product", expression = "java(resolveProduct(storeProductCreateDto.getProductId(), productRepository))")
    @Mapping(target = "competitor", expression = "java(resolveCompetitor(storeProductCreateDto.getCompetitorId(), competitorRepository))")
    @Mapping(target = "crawlState", constant = "SCHEDULED") // Default state
    @Mapping(target = "interval", expression = "java(java.time.Duration.ofHours(4))") // Example default
    @Mapping(target = "lastCrawled", expression = "java(java.time.ZonedDateTime.now().minusDays(1))") // Default value
    @Mapping(target = "nextCrawl", expression = "java(java.time.ZonedDateTime.now())") // Default value
    @Mapping(target = "retryAttempts", constant = "0L")
    @Mapping(target = "pauseRequested", constant = "false")
    StoreProduct toEntity(StoreProductCreateDto storeProductCreateDto,
                          @Context ProductRepository productRepository,
                          @Context CompetitorRepository competitorRepository);

    default Product resolveProduct(Long productId, ProductRepository productRepository) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product with ID " + productId + " not found."));
    }

    default Competitor resolveCompetitor(Long competitorId, CompetitorRepository competitorRepository) {
        return competitorRepository.findById(competitorId)
            .orElseThrow(() -> new EntityNotFoundException("Competitor with ID " + competitorId + " not found."));
    }
}
