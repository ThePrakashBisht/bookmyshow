package com.bookmyshow.userservice.config;

import com.bookmyshow.userservice.entity.Role;
import com.bookmyshow.userservice.entity.RoleName;
import com.bookmyshow.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

//    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing default data...");

        // Create roles if they don't exist
        createRoleIfNotExists(RoleName.ROLE_USER);
        createRoleIfNotExists(RoleName.ROLE_ADMIN);
        createRoleIfNotExists(RoleName.ROLE_ORGANIZER);

        log.info("Default data initialization completed!");
    }

    private void createRoleIfNotExists(RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = Role.builder()
                    .name(roleName)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }
}