package ase.cogniprice.unitTest.applicationUserTest.serviceTest;

import ase.cogniprice.controller.dto.application.user.ApplicationUserRegistrationDto;
import ase.cogniprice.controller.mapper.ApplicationUserMapper;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.exception.EmailAlreadyExistsException;
import ase.cogniprice.exception.UsernameAlreadyExistsException;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.security.JwtTokenizer;
import ase.cogniprice.service.implementation.ApplicationUserImpl;
import ase.cogniprice.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationUserRegistrationTest {
    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenizer jwtTokenizer;

    @Mock
    private ApplicationUserMapper applicationUserMapper;

    @InjectMocks
    private ApplicationUserImpl applicationUserService;

    private ApplicationUserRegistrationDto registrationDto;
    private ApplicationUser testUser;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        registrationDto = new ApplicationUserRegistrationDto();
        registrationDto.setUsername("testUser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");

        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
    }

    @Test
    void registerUser_success() throws UsernameAlreadyExistsException {
        doReturn(null, testUser).when(applicationUserRepository).findApplicationUserByEmail(registrationDto.getEmail());
        when(applicationUserRepository.findApplicationUserByUsername(registrationDto.getUsername())).thenReturn(null).thenReturn(testUser);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(applicationUserMapper.toEntity(registrationDto)).thenReturn(testUser);
        when(jwtTokenizer.getAuthToken(testUser.getUsername(), testUser.getId(), List.of("ROLE_USER"))).thenReturn("mockJwtToken");


        String token = applicationUserService.registerUser(registrationDto);

        assertNotNull(token, "Token should not be null upon successful registration");
        assertEquals("mockJwtToken", token, "Expected token returned from JwtTokenizer");

        verify(applicationUserRepository).save(testUser);
        verify(applicationUserRepository, times(1)).findApplicationUserByEmail(registrationDto.getEmail());
    }

    @Test
    void registerUser_usernameAlreadyExists() {
        when(applicationUserRepository.findApplicationUserByUsername(registrationDto.getUsername())).thenReturn(testUser);

        UsernameAlreadyExistsException exception = assertThrows(UsernameAlreadyExistsException.class,
                () -> applicationUserService.registerUser(registrationDto),
                "Expected UsernameAlreadyExistsException when username already exists");

        assertEquals("User with username " + registrationDto.getUsername() + " already exists.", exception.getMessage());
    }

    @Test
    void registerUser_emailAlreadyExists() {
        when(applicationUserRepository.findApplicationUserByEmail(registrationDto.getEmail())).thenReturn(testUser);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> applicationUserService.registerUser(registrationDto),
                "Expected EmailAlreadyExistsException when email already exists");

        assertEquals("User with email " + registrationDto.getEmail() + " already exists.", exception.getMessage());
    }

}
