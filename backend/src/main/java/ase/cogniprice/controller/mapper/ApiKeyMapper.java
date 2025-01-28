package ase.cogniprice.controller.mapper;

import ase.cogniprice.controller.dto.api.key.ApiKeyDetailsDto;
import ase.cogniprice.entity.ApiKey;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiKeyMapper {

    ApiKeyDetailsDto toDetailsDto(ApiKey apiKey);
}