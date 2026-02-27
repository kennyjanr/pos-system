package com.example.pos.config;

import com.example.pos.entity.Role;
import com.example.pos.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // Create roles if they don't exist
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_MANAGER");
        createRoleIfNotExists("ROLE_CASHIER");
    }

    private void createRoleIfNotExists(String roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = Role.builder().name(roleName).build();
            return roleRepository.save(role);
        });
    }
}
