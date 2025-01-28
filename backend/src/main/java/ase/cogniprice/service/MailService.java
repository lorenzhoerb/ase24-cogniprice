package ase.cogniprice.service;

import ase.cogniprice.entity.ApplicationUser;

public interface MailService {

    /**
     * Sends a password reset email to the specified user with a token link to reset their password.
     * The email contains a URL that directs the user to a page where they can enter a new password using the provided token.
     *
     * @param user the user to whom the password reset email will be sent.
     * @param token the password reset token to include in the email.
     */
    void sendRestPasswordEmail(ApplicationUser user, String token);
}
