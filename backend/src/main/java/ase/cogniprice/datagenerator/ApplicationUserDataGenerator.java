package ase.cogniprice.datagenerator;


import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.type.Role;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;

@Profile({"generateData", "generateUsers", "generateDemoData"})
@Component
public class ApplicationUserDataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ApplicationUserDataGenerator(ApplicationUserRepository applicationUserRepository,
                                        PasswordEncoder passwordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostConstruct
    private void generateApplicationUser() {
        if (applicationUserRepository.findApplicationUserByEmail("user@email.com") == null) {
            ApplicationUser user = new ApplicationUser();
            user.setUsername("regularUser");
            user.setPassword(passwordEncoder.encode("password!"));
            user.setFirstName("User");
            user.setLastName("Test");
            user.setEmail("user@email.com");
            user.setLoginAttempts(0L);
            user.setUserLocked(false);
            user.setRole(Role.USER); // assuming Role.USER exists in your Role enum
            user.setStoreProducts(new HashSet<>());
            applicationUserRepository.save(user);
            LOG.info("Created regular user with email: {}", user.getEmail());
        }

        if (applicationUserRepository.findApplicationUserByEmail("admin@email.com") == null) {
            ApplicationUser admin = new ApplicationUser();
            admin.setUsername("adminUser");
            admin.setPassword(passwordEncoder.encode("adminPassword!"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@email.com");
            admin.setLoginAttempts(0L);
            admin.setUserLocked(false);
            admin.setRole(Role.ADMIN); // assuming Role.ADMIN exists in your Role enum
            applicationUserRepository.save(admin);
            LOG.info("Created admin user with email: {}", admin.getEmail());
        }
    }
}
