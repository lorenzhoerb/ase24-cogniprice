package ase.cogniprice.controller.mapper;

import ase.cogniprice.controller.dto.application.user.ApplicationUserDetailsDto;
import ase.cogniprice.controller.dto.application.user.ApplicationUserRegistrationDto;
import ase.cogniprice.entity.ApplicationUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ApplicationUserMapper {

    @Mapping(target = "loginAttempts", constant = "0L") // default value for loginAttempts
    @Mapping(target = "userLocked", constant = "false") // default value for userLocked
    @Mapping(target = "role", constant = "USER") // default role as USER, make sure Role is an enum
    @Mapping(target = "storeProducts", expression = "java(new java.util.HashSet<>())") // Initialize storeProducts
    ApplicationUser toEntity(ApplicationUserRegistrationDto registrationDto);

    @Named("simpleApplicationUserToApplicationUserDetailsDto")
    ApplicationUserDetailsDto applicationUserToApplicationUserDetailsDto(ApplicationUser applicationUser);
}
