package ase.cogniprice.controller;

import ase.cogniprice.config.properties.SecurityProperties;
import ase.cogniprice.controller.dto.api.key.ApiKeyDetailsDto;
import ase.cogniprice.controller.dto.application.user.ApplicationUserDetailsDto;
import ase.cogniprice.controller.dto.application.user.ApplicationUserEditDto;
import ase.cogniprice.controller.dto.password.PasswordDto;
import ase.cogniprice.controller.mapper.ApplicationUserMapper;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.exception.EmailAlreadyExistsException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.UsernameAlreadyExistsException;
import ase.cogniprice.service.ApplicationUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("/api/users")
public class ApplicationUserController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ApplicationUserService applicationUserService;
    private final SecurityProperties securityProperties;
    private final ApplicationUserMapper applicationUserMapper;

    static final String BASE_PATH = "/api/users";

    @Autowired
    public ApplicationUserController(
            ApplicationUserService applicationUserService,
            SecurityProperties securityProperties,
            ApplicationUserMapper applicationUserMapper) {
        this.applicationUserService = applicationUserService;
        this.securityProperties = securityProperties;
        this.applicationUserMapper = applicationUserMapper;
    }

    @Secured("ROLE_USER")
    @PutMapping("/edit")
    @Operation(summary = "Edit a user based on its id accessed from the authorization token")
    public ResponseEntity<String> editUser(
            @Valid @RequestBody ApplicationUserEditDto applicationUserEditDto,
            @AuthenticationPrincipal String username,
            HttpServletRequest request) {
        LOG.info("PUT /api/users/edit body: {}", applicationUserEditDto);
        try {
            ApplicationUser user = applicationUserService.getApplicationUserByUsername(username);
            String newToken = this.applicationUserService.editApplicationUser(applicationUserEditDto, user.getId());
            if (newToken != null) {
                return new ResponseEntity<>(newToken, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            LOG.debug("User to be edited has not be found.");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @Secured("ROLE_USER")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete the account that is logged in based on the authorization token.")
    public void deleteApplicationUser(HttpServletRequest request, @AuthenticationPrincipal String username) {
        LOG.info("DELETE {}", BASE_PATH);
        ApplicationUser user = applicationUserService.getApplicationUserByUsername(username);
        try {
            this.applicationUserService.deleteApplicationUser(user.getId());
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/details")
    @Operation(summary = "Get user information based on the authorization token containing the user id")
    public ApplicationUserDetailsDto getDetails(HttpServletRequest request, @AuthenticationPrincipal String username) {
        LOG.info("GET /api/users/details");
        try {
            return this.applicationUserMapper.applicationUserToApplicationUserDetailsDto(
                    this.applicationUserService.getApplicationUserByUsername(username)
            );
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            throw new ResponseStatusException(status, e.getMessage());
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/{username}")
    @Operation(summary = "Get user information by username")
    public ApplicationUserDetailsDto getUserByUsername(@PathVariable String username) {
        LOG.info("GET /api/users/" + username);
        try {
            return this.applicationUserMapper.applicationUserToApplicationUserDetailsDto(
                    this.applicationUserService.getApplicationUserByUsername(username)
            );
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            throw new ResponseStatusException(status, e.getMessage());
        }
    }

    @Secured({"ROLE_USER"})
    @PostMapping("/changePassword")
    public ResponseEntity<String>  changePassword(@RequestBody @Valid PasswordDto passwordDto,
                                                  @AuthenticationPrincipal String username) {
        LOG.info("POST {}/changePassword body: {}", BASE_PATH, passwordDto);

        applicationUserService.changePassword(username, passwordDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Secured("ROLE_USER")
    @PostMapping("/api-key/generate")
    @Operation(summary = "Generate a new API Key for the logged in user and returns it")
    public ResponseEntity<String> generateApiKey(@AuthenticationPrincipal String username) {
        LOG.info("POST {}/api-key/generate", BASE_PATH);

        String newApiKey = applicationUserService.generateApiKey(username);
        return ResponseEntity.status(HttpStatus.CREATED).body(newApiKey);
    }

    @Secured("ROLE_USER")
    @GetMapping("/api-key")
    @Operation(summary = "Get api key details for the logged in user")
    public ResponseEntity<ApiKeyDetailsDto> getApiKeyDetails(@AuthenticationPrincipal String username) {
        LOG.info("GET {}/api-key", BASE_PATH);

        ApiKeyDetailsDto apiKey = applicationUserService.getApiKeyDetails(username);
        return ResponseEntity.status(HttpStatus.OK).body(apiKey);
    }
}
