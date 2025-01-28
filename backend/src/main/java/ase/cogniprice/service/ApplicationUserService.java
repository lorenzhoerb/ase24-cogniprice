package ase.cogniprice.service;

import ase.cogniprice.controller.dto.api.key.ApiKeyDetailsDto;
import ase.cogniprice.controller.dto.application.user.ApplicationUserEditDto;
import ase.cogniprice.controller.dto.application.user.ApplicationUserRegistrationDto;
import ase.cogniprice.controller.dto.authentication.AuthRequest;
import ase.cogniprice.controller.dto.password.PasswordDto;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.exception.EmailAlreadyExistsException;
import ase.cogniprice.exception.ExpiredTokenException;
import ase.cogniprice.exception.InvalidPasswordException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.UserLockedException;
import ase.cogniprice.exception.UsernameAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface ApplicationUserService extends UserDetailsService {

    /**
     * Registers a new user with the provided registration details.
     *
     * @param applicationUserRegistrationDto the registration details.
     * @return a JWT token for the newly registered user.
     * @throws UsernameAlreadyExistsException if the username already exists.
     * @throws EmailAlreadyExistsException    if the email already exists.
     */
    String registerUser(ApplicationUserRegistrationDto applicationUserRegistrationDto)
        throws UsernameAlreadyExistsException, EmailAlreadyExistsException;

    /**
     * Retrieves an application user by their ID.
     *
     * @param id the ID of the user.
     * @return the {@code ApplicationUser}.
     * @throws NotFoundException if the user is not found.
     */
    ApplicationUser getApplicationUserById(Long id);

    /**
     * Retrieves an application user by their username.
     *
     * @param username the email address of the user.
     * @return the {@code ApplicationUser}.
     * @throws NotFoundException if the user is not found.
     */
    ApplicationUser getApplicationUserByUsername(String username);

    /**
     * Authenticates a user based on their login credentials.
     *
     * @param authRequest the authentication request containing username and password.
     * @return a JWT token for the authenticated user.
     * @throws NotFoundException        if the user is not found.
     * @throws UserLockedException      if the user account is locked.
     * @throws InvalidPasswordException if the password is incorrect.
     */
    String loginApplicationUser(AuthRequest authRequest);


    /**
     * Edit ApplicationUser by Id.
     *
     * @param applicationUserEditDto    The new user details.
     * @param id                        Id of the user.
     * @return a JWT if the username has changed.
     * @throws NotFoundException        If the user is not found.
     */
    String editApplicationUser(ApplicationUserEditDto applicationUserEditDto, Long id);


    /**
     * Delete ApplicationUser by Id.
     *
     * @param id    The id of the user
     */
    void deleteApplicationUser(Long id);


    /**
     * Resets the password for the user identified by the given email.
     * A password reset token is generated and an email is sent to the user with instructions to reset their password.
     *
     * @param userEmail the email of the user who wants to reset their password.
     * @throws EntityNotFoundException if no user is found with the provided email address.
     */
    void resetPassword(String userEmail) throws EntityNotFoundException;


    /**
     * Changes the password of the user based on the provided password DTO.
     * The password reset token is validated before the password change.
     *
     * @param passwordDto the DTO containing the reset token and the new password.
     * @throws EntityNotFoundException if the provided token or the user is not found.
     * @throws ExpiredTokenException if the provided reset token has expired.
     */
    void changePassword(PasswordDto passwordDto) throws EntityNotFoundException;

    /**
     * Changes the password of the user based on the username and the provided password DTO.
     *
     * @param passwordDto the DTO containing the reset token and the new password.
     * @param username the username of the user
     * @throws EntityNotFoundException if the provided token or the user is not found.
     * @throws InvalidPasswordException when the previous password does not match the one in passwordDto
     */

    void changePassword(String username, PasswordDto passwordDto) throws EntityNotFoundException;

    /**
     * Generates a new api key for the user.
     *
     * @param username the username of the user.
     * @return returns the accessKey as a string.
     * @throws EntityNotFoundException if the provided user is not found.
     */
    String generateApiKey(String username) throws EntityNotFoundException;


    /**
     * Get the api key details for the connected user.
     *
     * @param username the username of the user.
     * @return ApiKeyDetailsDto containing the creation/expiry date and the masked access key.
     * @throws EntityNotFoundException if the access key or the user is not found.
     */
    ApiKeyDetailsDto getApiKeyDetails(String username) throws EntityNotFoundException;
}
