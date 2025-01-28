package ase.cogniprice.service.implementation;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.exception.EmailSendingException;
import ase.cogniprice.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    private final Environment env;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, Environment env) {
        this.mailSender = mailSender;
        this.env = env;
    }

    @Override
    public void sendRestPasswordEmail(ApplicationUser user, String token) {
        LOG.trace("sendRestPasswordEmail");

        try {
            this.mailSender.send(constructResetTokenEmail(env.getProperty("base.url"), token, user));
        } catch (MessagingException e) {
            LOG.error("Error while sending email to {}: {}", user.getEmail(), e.getMessage());
            throw new EmailSendingException("Failed to send password reset email to " + user.getEmail(), e);
        }
    }

    private MimeMessage constructResetTokenEmail(String contextPath, String token, ApplicationUser user) throws MessagingException {
        LOG.trace("constructResetTokenEmail");

        String url = contextPath + "/change-password?token=" + token;
        String subject = "Reset Your Password";
        String message =
            "<div style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "<h2 style=\"color: #0056b3;\">Hello, " + user.getUsername() + "!</h2>" +
                "<p>We received a request to reset your password. If this was you, click the button below to reset your password:</p>" +
                "<div style=\"text-align: center; margin: 20px 0;\">" +
                "<a href=\"" + url + "\" style=\"background-color: #007bff; color: white; text-decoration: none; padding: 10px 20px; border-radius: 5px; font-size: 16px;\">Reset Password</a>" +
                "</div>" +
                "<p>If you didn’t request a password reset, you can safely ignore this email. Your password will remain the same.</p>" +
                "<p style=\"margin-top: 20px;\">Thank you,<br>The Support Team</p>" +
                "</div>";

        MimeMessage email = this.mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(email, true);

        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(message, true); // Enable HTML content

        return email;
    }
}
