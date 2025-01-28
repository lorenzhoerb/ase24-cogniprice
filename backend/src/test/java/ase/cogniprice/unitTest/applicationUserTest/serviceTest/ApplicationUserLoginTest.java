package ase.cogniprice.unitTest.applicationUserTest.serviceTest;

import ase.cogniprice.controller.dto.authentication.AuthRequest;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.exception.InvalidPasswordException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.UserLockedException;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.security.JwtTokenizer;
import ase.cogniprice.service.implementation.ApplicationUserImpl;
import ase.cogniprice.type.Role;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationUserLoginTest {

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenizer jwtTokenizer;

    @InjectMocks
    private ApplicationUserImpl applicationUserService;

    private ApplicationUser testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testUser");
        testUser.setPassword("encodedPassword");  // This would be the encoded password
        testUser.setLoginAttempts(0L);
        testUser.setUserLocked(false);
        testUser.setRole(Role.USER);
    }

    @Test
    void loginApplicationUser_success() throws NotFoundException, InvalidPasswordException, UserLockedException {
        // Given
        AuthRequest authRequest = new AuthRequest("testUser", "correctPassword");
        when(applicationUserRepository.findApplicationUserByUsername(authRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        List<String> roles = new ArrayList<String>();
        roles.add("ROLE_USER");
        when(jwtTokenizer.getAuthToken(testUser.getUsername(), testUser.getId(), roles)).thenReturn("mockJwtToken");

        // When
        String token = applicationUserService.loginApplicationUser(authRequest);

        // Then
        assertNotNull(token, "Token should not be null on successful login");
        assertEquals("mockJwtToken", token, "Expected token returned from JwtTokenizer");
    }

    @Test
    void loginApplicationUser_invalidPassword() {
        // Given
        AuthRequest authRequest = new AuthRequest("test@example.com", "wrongPassword");
        when(applicationUserRepository.findApplicationUserByUsername(authRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
            () -> applicationUserService.loginApplicationUser(authRequest),
            "Expected InvalidPasswordException when password is incorrect");

        assertEquals("Password or Username incorrect", exception.getMessage());
    }

    @Test
    void loginApplicationUser_userNotFound() {
        // Given
        AuthRequest authRequest = new AuthRequest("nonexistent", "somePassword");
        when(applicationUserRepository.findApplicationUserByUsername(authRequest.getUsername())).thenReturn(null);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> applicationUserService.loginApplicationUser(authRequest),
            "Expected NotFoundException when user does not exist");

        assertEquals("User with username nonexistent not found.", exception.getMessage());
    }

    @Test
    void loginApplicationUser_accountLocked() {
        // Given
        AuthRequest authRequest = new AuthRequest("test@example.com", "correctPassword");
        testUser.setUserLocked(true);
        when(applicationUserRepository.findApplicationUserByUsername(authRequest.getUsername())).thenReturn(testUser);

        // When & Then
        UserLockedException exception = assertThrows(UserLockedException.class,
            () -> applicationUserService.loginApplicationUser(authRequest),
            "Expected UserLockedException when user account is locked");

        assertEquals("Account locked", exception.getMessage());
    }

    @Test
    void loginApplicationUser_incrementLoginAttemptsAndLockAccount() {
        // Given
        AuthRequest authRequest = new AuthRequest("test@example.com", "wrongPassword");
        testUser.setLoginAttempts(4L);
        when(applicationUserRepository.findApplicationUserByUsername(authRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        UserLockedException exception = assertThrows(UserLockedException.class,
            () -> applicationUserService.loginApplicationUser(authRequest),
            "Expected UserLockedException when login attempts reach the limit");

        assertEquals("Account locked due to too many failed attempts.", exception.getMessage());
        verify(applicationUserRepository).updateUserLockedStatus(testUser.getId(), true);
    }
}
