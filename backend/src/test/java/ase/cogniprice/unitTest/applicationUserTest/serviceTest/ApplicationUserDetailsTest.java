package ase.cogniprice.unitTest.applicationUserTest.serviceTest;

import ase.cogniprice.controller.dto.application.user.ApplicationUserEditDto;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.exception.EmailAlreadyExistsException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.UsernameAlreadyExistsException;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApplicationUserDetailsTest {

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @InjectMocks
    private ApplicationUserImpl applicationUserService;

    @Mock
    private JwtTokenizer jwtTokenizer;

    private ApplicationUser testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");  // This would be the encoded password
        testUser.setLoginAttempts(0L);
        testUser.setUserLocked(false);
        testUser.setRole(Role.USER);
    }

    // Get User details tests
    @Test
    void testGetApplicationUserById_Success() throws NotFoundException {
        // Mock repository behavior
        ApplicationUser mockUser;
        when(applicationUserRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Call the service method
        ApplicationUser result = applicationUserService.getApplicationUserById(1L);

        // Assertions
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());

        // Verify interactions
        verify(applicationUserRepository, times(1)).findById(1L);
    }

    @Test
    void testGetApplicationUserById_NotFound() {
        // Mock repository behavior to return empty
        when(applicationUserRepository.findById(1L)).thenReturn(Optional.empty());

        // Call the service method and assert exception
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            applicationUserService.getApplicationUserById(1L);
        });

        assertEquals("ApplicationUser with ID 1 not found", exception.getMessage());

        // Verify interactions
        verify(applicationUserRepository, times(1)).findById(1L);
    }

    /* Edit ApplicationUser Tests */

    @Test
    void testEditApplicationUserSuccess() throws NotFoundException {
        ApplicationUserEditDto editDto = new ApplicationUserEditDto(
                "updated@test.com",
                "updatedUsername",
                "UpdatedFirstName",
                "UpdatedLastName"
        );

        when(applicationUserRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(applicationUserRepository.findApplicationUserByEmail(editDto.getEmail())).thenReturn(null);
        when(applicationUserRepository.findApplicationUserByUsername("updatedUsername"))
                .thenReturn(null) // First call (conflict check)
                .thenReturn(testUser);
        when(jwtTokenizer.getAuthToken(
                testUser.getUsername(), testUser.getId(), List.of("ROLE_USER"))).thenReturn("mockJwtToken");

        assertDoesNotThrow(() -> applicationUserService.editApplicationUser(editDto, 1L));

        assertEquals("updated@test.com", testUser.getEmail());
        assertEquals("updatedUsername", testUser.getUsername());
        assertEquals("UpdatedFirstName", testUser.getFirstName());
        assertEquals("UpdatedLastName", testUser.getLastName());
        verify(applicationUserRepository, times(1)).save(testUser);
    }

    @Test
    void testEditApplicationUserEmailConflict() {
        // Arrange
        ApplicationUserEditDto editDto = new ApplicationUserEditDto(
                "existing@test.com",
                "updatedUsername",
                "UpdatedFirstName",
                "UpdatedLastName"
        );

        when(applicationUserRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(applicationUserRepository.findApplicationUserByEmail(editDto.getEmail()))
                .thenReturn(new ApplicationUser());

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            applicationUserService.editApplicationUser(editDto, 1L);
        });

        assertTrue(exception.getMessage().contains("User with email existing@test.com already exists."));
        verify(applicationUserRepository, never()).save(any());
    }

    @Test
    void testEditApplicationUserUsernameConflict() {
        // Arrange
        ApplicationUserEditDto editDto = new ApplicationUserEditDto(
                "updated@test.com",
                "existingUsername",
                "UpdatedFirstName",
                "UpdatedLastName"
        );

        when(applicationUserRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(applicationUserRepository.findApplicationUserByUsername(editDto.getUsername()))
                .thenReturn(new ApplicationUser());

        UsernameAlreadyExistsException exception = assertThrows(UsernameAlreadyExistsException.class, () -> {
            applicationUserService.editApplicationUser(editDto, 1L);
        });

        assertTrue(exception.getMessage().contains("User with username existingUsername already exists."));
        verify(applicationUserRepository, never()).save(any());
    }

    /* Delete ApplicationUser */

    @Test
    void testDeleteApplicationUserSuccess() {
        when(applicationUserRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(applicationUserRepository).deleteById(1L);

        assertDoesNotThrow(() -> applicationUserService.deleteApplicationUser(1L));

        verify(applicationUserRepository, times(1)).findById(1L);
        verify(applicationUserRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteApplicationUserNotFound() {
        when(applicationUserRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            applicationUserService.deleteApplicationUser(1L);
        });

        assertEquals("Could not find user with id: 1", exception.getMessage());

        verify(applicationUserRepository, times(1)).findById(1L);
        verify(applicationUserRepository, never()).deleteById(anyLong());
    }

}
