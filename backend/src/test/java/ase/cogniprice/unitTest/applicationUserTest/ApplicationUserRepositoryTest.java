package ase.cogniprice.unitTest.applicationUserTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.type.Role;
import ase.cogniprice.repository.ApplicationUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // Ensures changes are rolled back after each test
class ApplicationUserRepositoryTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    private ApplicationUser user;

    @BeforeEach
    public void setUp() {
        user = new ApplicationUser();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        user.setRole(Role.USER);
    }

    @AfterEach
    public void tearDown() {
        applicationUserRepository.delete(user);
    }

    @Test
    void testCreateApplicationUser() {
        ApplicationUser savedUser = applicationUserRepository.save(user);
        assertNotNull(savedUser.getId(), "User ID should not be null after saving");
        assertEquals("testuser", savedUser.getUsername(), "Username should be 'testuser'");
    }

    @Test
    void testReadApplicationUser() {
        ApplicationUser savedUser = applicationUserRepository.save(user);
        Optional<ApplicationUser> foundUser = applicationUserRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent(), "User should be found by ID");
        assertEquals("testuser", foundUser.get().getUsername(), "Username should match");
        assertEquals("testuser@example.com", foundUser.get().getEmail(), "Email should match");
    }

    @Test
    void testUpdateApplicationUser() {
        ApplicationUser savedUser = applicationUserRepository.save(user);
        savedUser.setFirstName("UpdatedFirstName");
        ApplicationUser updatedUser = applicationUserRepository.save(savedUser);
        assertEquals("UpdatedFirstName", updatedUser.getFirstName(), "First name should be updated");
    }

    @Test
    void testDeleteApplicationUser() {
        ApplicationUser savedUser = applicationUserRepository.save(user);
        applicationUserRepository.delete(savedUser);
        Optional<ApplicationUser> deletedUser = applicationUserRepository.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent(), "User should be deleted and not found");
    }

    @Test
    void testUpdateUserLockedStatus() {
        ApplicationUser savedUser = applicationUserRepository.save(user);
        int updatedRows = applicationUserRepository.updateUserLockedStatus(savedUser.getId(), true);
        assertEquals(1, updatedRows, "One row should be updated");

        Optional<ApplicationUser> foundUser = applicationUserRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent(), "User should be present");
        assertTrue(foundUser.get().isUserLocked(), "User should be locked");

        updatedRows = applicationUserRepository.updateUserLockedStatus(savedUser.getId(), false);
        assertEquals(1, updatedRows, "One row should be updated");

        foundUser = applicationUserRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent(), "User should be present");
        assertFalse(foundUser.get().isUserLocked(), "User should be unlocked");
    }



    @Test
    void testUpdateUserLoginAttempts() {
        ApplicationUser savedUser = applicationUserRepository.save(user);
        // Update login attempts
        applicationUserRepository.updateUserLoginAttempts(savedUser.getId(), 3L);
        Optional<ApplicationUser> foundUser = applicationUserRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent(), "User should be present");
        assertEquals(3L, foundUser.get().getLoginAttempts(), "Login attempts should be updated to 3");
    }

    @Test
    void testIncrementLoginAttempts() {
        ApplicationUser savedUser = applicationUserRepository.save(user);
        // Increment login attempts
        int updatedRows = applicationUserRepository.incrementLoginAttempts(savedUser.getId());
        assertEquals(1, updatedRows, "One row should be updated");

        Optional<ApplicationUser> foundUser = applicationUserRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent(), "User should be present");
        assertEquals(1L, foundUser.get().getLoginAttempts(), "Login attempts should be incremented to 1");

        // Increment login attempts again
        applicationUserRepository.incrementLoginAttempts(savedUser.getId());
        foundUser = applicationUserRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent(), "User should be present");
        assertEquals(2L, foundUser.get().getLoginAttempts(), "Login attempts should be incremented to 2");
    }
}
