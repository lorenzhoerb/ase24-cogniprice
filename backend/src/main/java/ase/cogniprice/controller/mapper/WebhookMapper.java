package ase.cogniprice.controller.mapper;

import ase.cogniprice.controller.dto.webhook.dto.WebhookRegistrationDto;
import ase.cogniprice.entity.Webhook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WebhookMapper {

    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())") // Auto-set creation timestamp
    @Mapping(target = "products", ignore = true) // Products can be set later if needed
    @Mapping(target = "applicationUser", ignore = true) // ApplicationUser is set in the service layer
    Webhook toEntity(WebhookRegistrationDto registrationDto);

    @Mapping(source = "callbackUrl", target = "callbackUrl")
    WebhookRegistrationDto toDto(Webhook webhook);
}
