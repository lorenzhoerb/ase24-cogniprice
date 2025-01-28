package ase.cogniprice.service.implementation;

import ase.cogniprice.controller.dto.api.key.ApiKeyDetailsDto;
import ase.cogniprice.controller.dto.application.user.ApplicationUserRegistrationDto;
import ase.cogniprice.controller.dto.application.user.ApplicationUserEditDto;
import ase.cogniprice.controller.dto.authentication.AuthRequest;
import ase.cogniprice.controller.dto.password.PasswordDto;
import ase.cogniprice.controller.mapper.ApiKeyMapper;
import ase.cogniprice.controller.mapper.ApplicationUserMapper;
import ase.cogniprice.entity.ApiKey;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.PasswordResetToken;
import ase.cogniprice.exception.EmailAlreadyExistsException;
import ase.cogniprice.exception.InvalidPasswordException;
import ase.cogniprice.exception.ExpiredTokenException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.UserLockedException;
import ase.cogniprice.exception.UsernameAlreadyExistsException;
import ase.cogniprice.repository.ApiKeyRepository;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.PasswordTokenRepository;
import ase.cogniprice.security.ApiKeyEncoder;
import ase.cogniprice.security.CustomUserDetails;
import ase.cogniprice.security.JwtTokenizer;
import ase.cogniprice.service.ApplicationUserService;
import ase.cogniprice.service.MailService;
import ase.cogniprice.type.Role;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApplicationUserImpl implements ApplicationUserService {

    private static final Integer API_KEY_VALIDITY_DAYS = 90;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserMapper applicationUserMapper;
    private final ApiKeyMapper apiKeyMapper;
    private final PasswordTokenRepository passwordTokenRepository;
    private final JwtTokenizer jwtTokenizer;
    private final ApiKeyEncoder apiKeyEncoder;
    private final MailService mailService;

    @Autowired
    public ApplicationUserImpl(ApplicationUserRepository applicationUserRepository,
                               PasswordEncoder passwordEncoder,
                               ApplicationUserMapper applicationUserMapper,
                               ApiKeyMapper apiKeyMapper,
                               PasswordTokenRepository passwordTokenRepository,
                               JwtTokenizer jwtTokenizer, ApiKeyEncoder apiKeyEncoder,
                               MailService mailService,
                               ApiKeyRepository apiKeyRepository) {
        this.applicationUserRepository = applicationUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationUserMapper = applicationUserMapper;
        this.apiKeyMapper = apiKeyMapper;
        this.passwordTokenRepository = passwordTokenRepository;
        this.jwtTokenizer = jwtTokenizer;
        this.apiKeyEncoder = apiKeyEncoder;
        this.mailService = mailService;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOG.trace("loadUserByUsername");
        try {
            ApplicationUser applicationUser = getApplicationUserByUsername(username);

            List<GrantedAuthority> grantedAuthorities;
            if (applicationUser.getRole() == Role.ADMIN) {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
            } else {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");
            }


            CustomUserDetails userDetails = new CustomUserDetails(applicationUser.getUsername(), applicationUser.getPassword(), !applicationUser.isUserLocked(), true, true, !applicationUser.isUserLocked(),
                    grantedAuthorities);
            userDetails.setId(applicationUser.getId());
            return userDetails;
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public String registerUser(ApplicationUserRegistrationDto applicationUserRegistrationDto) throws UsernameAlreadyExistsException {
        LOG.trace("registerUser");

        // Check if the user already exists
        this.checkIfEmailAlreadyExists(applicationUserRegistrationDto.getEmail());
        this.checkIfUsernameAlreadyExists(applicationUserRegistrationDto.getUsername());

        // Encrypt the password before saving
        String encodedPassword = passwordEncoder
                .encode(applicationUserRegistrationDto.getPassword());

        // Create and set user attributes
        ApplicationUser newApplicationUser = applicationUserMapper
                .toEntity(applicationUserRegistrationDto);

        // Set the encoded password to the user entity
        newApplicationUser.setPassword(encodedPassword);

        // Save user to database
        applicationUserRepository.save(newApplicationUser);

        UserDetails userDetails = loadUserByUsername(newApplicationUser.getUsername());
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Long userId = newApplicationUser.getId();

        return jwtTokenizer.getAuthToken(applicationUserRegistrationDto.getUsername(),
            userId,
            roles);
    }

    @Override
    @Transactional
    public String loginApplicationUser(AuthRequest authRequest) throws EntityNotFoundException, UserLockedException, InvalidPasswordException {
        LOG.trace("loginApplicationUser");

        ApplicationUser applicationUser = this.getApplicationUserByUsername(authRequest.getUsername());

        this.handlePassword(applicationUser, authRequest.getPassword());

        Long userId = applicationUser.getId();

        UserDetails userDetails = this.loadUserByUsername(applicationUser.getUsername());
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return jwtTokenizer.getAuthToken(applicationUser.getUsername(),
            userId,
            roles);
    }

    @Override
    @Transactional
    public ApplicationUser getApplicationUserById(Long id) throws EntityNotFoundException {
        LOG.trace("getApplicationUserById");
        return applicationUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ApplicationUser with ID " + id + " not found"));
    }

    @Override
    public ApplicationUser getApplicationUserByUsername(String username) throws EntityNotFoundException {
        LOG.trace("getApplicationUserByUsername");
        ApplicationUser applicationUser =
            applicationUserRepository.findApplicationUserByUsername(username);
        if (applicationUser != null) {
            return applicationUser;
        } else {
            throw new EntityNotFoundException("User with username " + username + " not found.");
        }
    }

    @Override
    public String editApplicationUser(ApplicationUserEditDto applicationUserEditDto, Long id) throws EntityNotFoundException {
        LOG.trace("Edit user: {} with id: {}", applicationUserEditDto, id);
        try {
            ApplicationUser applicationUser = getApplicationUserById(id);
            boolean usernameChanged = false;
            if (!applicationUser.getEmail().equals(applicationUserEditDto.getEmail())) {
                this.checkIfEmailAlreadyExists(applicationUserEditDto.getEmail());
            }
            if (!applicationUser.getUsername().equals(applicationUserEditDto.getUsername())) {
                this.checkIfUsernameAlreadyExists(applicationUserEditDto.getUsername());
                usernameChanged = true;
            }

            applicationUser.setEmail(applicationUserEditDto.getEmail());
            applicationUser.setUsername(applicationUserEditDto.getUsername());
            applicationUser.setFirstName(applicationUserEditDto.getFirstName());
            applicationUser.setLastName(applicationUserEditDto.getLastName());
            applicationUserRepository.save(applicationUser);

            if (usernameChanged) {
                UserDetails userDetails = loadUserByUsername(applicationUserEditDto.getUsername());
                List<String> roles = userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

                return jwtTokenizer.getAuthToken(applicationUser.getUsername(), id, roles);
            }
            return null;
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Could not find user with id: " + id);
        }
    }

    @Override
    public void deleteApplicationUser(Long id) {
        LOG.trace("deleteApplicationUser with id: {}", id);
        Optional<ApplicationUser> applicationUser = applicationUserRepository.findById(id);
        if (applicationUser.isEmpty()) {
            throw new EntityNotFoundException("Could not find user with id: " + id);
        }
        try {
            // todo: Check when all backend implemented if all the cascades work
            applicationUserRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw new IllegalStateException("Error while deleting user with id: " + id, e);
        } catch (Exception e) {
            throw new EntityNotFoundException("Could not delete user with id: " + id);
        }
    }

    @Override
    public void resetPassword(String userEmail) throws EntityNotFoundException {
        LOG.trace("resetPassword");

        ApplicationUser applicationUser = this.getApplicationUserByEmail(userEmail);

        String token = UUID.randomUUID().toString();
        this.createPasswordResetTokenForUser(applicationUser, token);

        this.mailService.sendRestPasswordEmail(applicationUser, token);

    }

    @Override
    public void changePassword(PasswordDto passwordDto) throws EntityNotFoundException, ExpiredTokenException {
        LOG.trace("changePassword");

        PasswordResetToken passwordResetToken = this.validateToken(passwordDto.getToken());
        ApplicationUser user = passwordResetToken.getUser();
        String encodedPassword = passwordEncoder
            .encode(passwordDto.getNewPassword());
        user.setPassword(encodedPassword);
        this.applicationUserRepository.save(user);
    }

    @Override
    public void changePassword(String username, PasswordDto passwordDto) throws EntityNotFoundException {

        ApplicationUser user = this.getApplicationUserByUsername(username);

        if (!passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        this.applicationUserRepository.save(user);

    }

    @Override
    public String generateApiKey(String username) throws EntityNotFoundException {
        ApplicationUser user = this.getApplicationUserByUsername(username);
        String rawKey = this.generateRawApiKey();
        LOG.debug("Generated api key: " + rawKey);

        ApiKey apiKey =  this.generateAndSetApiKey(user, rawKey);
        user.setApiKey(apiKey);
        applicationUserRepository.save(user);
        return rawKey;
    }

    @Override
    public ApiKeyDetailsDto getApiKeyDetails(String username) throws EntityNotFoundException {
        ApplicationUser user = this.getApplicationUserByUsername(username);
        ApiKey apiKey = user.getApiKey();
        if (apiKey == null) {
            throw new EntityNotFoundException("API Key not found for user: " + username);
        }
        return apiKeyMapper.toDetailsDto(apiKey);
    }

    private String generateRawApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private ApiKey generateAndSetApiKey(ApplicationUser user, String rawKey) {
        ApiKey apiKey = getUserApiKey(user);
        String hashedKey = apiKeyEncoder.encodeApiKey(rawKey);
        apiKey.setMaskedKey(rawKey);
        apiKey.setAccessKey(hashedKey);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(LocalDateTime.now().plusDays(API_KEY_VALIDITY_DAYS));
        apiKey.setApplicationUser(user);
        return apiKey;
    }

    private ApiKey getUserApiKey(ApplicationUser user) {
        ApiKey apiKey = user.getApiKey();
        if (apiKey == null) {
            return new ApiKey();
        }
        return apiKey;
    }


    private ApplicationUser getApplicationUserByEmail(String email) throws EntityNotFoundException {
        LOG.trace("getApplicationUserByEmail");
        ApplicationUser applicationUser =
            applicationUserRepository.findApplicationUserByEmail(email);

        if (applicationUser != null) {
            return applicationUser;
        } else {
            throw new EntityNotFoundException("User with email " + email + " not found.");
        }
    }


    private PasswordResetToken validateToken(String token) throws EntityNotFoundException, ExpiredTokenException {
        LOG.trace("validateToken");

        PasswordResetToken passwordResetToken = this.passwordTokenRepository.findByToken(token);

        if (passwordResetToken == null) {
            throw new EntityNotFoundException("Token not found");
        } else if (passwordResetToken.isExpired()) {
            throw new ExpiredTokenException("Token is expired");
        }
        return passwordResetToken;
    }

    private void createPasswordResetTokenForUser(ApplicationUser user, String token) {
        LOG.trace("createPasswordResetTokenForUser");

        PasswordResetToken passwordResetToken = passwordTokenRepository.findByUser(user);

        if (passwordResetToken != null) {
            passwordResetToken.update(token);
            this.passwordTokenRepository.save(passwordResetToken);
        } else {
            passwordResetToken = new PasswordResetToken(user, token);
            // no need to save, because of Cascade.ALL in ApplicationUser
        }
        user.setPasswordResetToken(passwordResetToken);
        this.applicationUserRepository.save(user);
    }

    private void checkIfUsernameAlreadyExists(String username) throws UsernameAlreadyExistsException {
        if (applicationUserRepository.findApplicationUserByUsername(
            username) != null) {
            throw new UsernameAlreadyExistsException("User with username "
                + username + " already exists.");
        }
    }

    private void checkIfEmailAlreadyExists(String email) throws EmailAlreadyExistsException {
        if (applicationUserRepository.findApplicationUserByEmail(
            email) != null) {
            throw new EmailAlreadyExistsException("User with email "
                + email + " already exists.");
        }
    }

    private void handlePassword(ApplicationUser applicationUser, String passwordAuth)
        throws InvalidPasswordException, UserLockedException {
        LOG.debug("handlePassword");

        if (applicationUser.isUserLocked()) {
            LOG.debug("Account locked");
            throw new UserLockedException("Account locked");
        }

        if (!passwordEncoder.matches(passwordAuth, applicationUser.getPassword())) {
            LOG.debug("Password not correct. Incrementing login attempts.");
            this.applicationUserRepository.incrementLoginAttempts(applicationUser.getId());
            applicationUser.incrementLoginAttempts();

            if (applicationUser.getLoginAttempts() >= 5) {
                LOG.debug("Account locked due to too many failed attempts.");
                applicationUser.setUserLocked(true);
                this.applicationUserRepository.updateUserLockedStatus(applicationUser.getId(), true);
                throw new UserLockedException("Account locked due to too many failed attempts.");
            }
            throw new InvalidPasswordException("Password or Username incorrect");
        }

        this.applicationUserRepository.updateUserLoginAttempts(applicationUser.getId(), 0L);
        LOG.debug("Password correct");
    }
}
