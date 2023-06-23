package ru.skypro.homework.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.security.AppUser;
import ru.skypro.homework.dto.account.RegisterReq;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.UserMapper;

import javax.annotation.PostConstruct;

@Service
public class AuthServiceImpl implements AuthService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserDetailsManager manager;

    private final PasswordEncoder encoder;

    private final UserMapper mapper;

    public AuthServiceImpl(UserDetailsManager manager,
                           PasswordEncoder passwordEncoder,
                           UserMapper mapper) {
        this.manager = manager;
        this.encoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    public boolean login(String userName, String password) {
        if (!manager.userExists(userName)) {
            LOGGER.warn("User does not exist");
            return false;
        }
        UserDetails userDetails = manager.loadUserByUsername(userName);
        LOGGER.info("User " + userName + " logged in");
        return encoder.matches(password, userDetails.getPassword());
    }

    @Override
    public boolean register(RegisterReq registerReq, Role role) {
        String userName = registerReq.getUsername();
        if (manager.userExists(userName)) {
            LOGGER.warn("User can not register, because user with email " + userName + " exists in the database");
            return false;
        }
        manager.createUser(
                new AppUser(
                        mapper.toUserEntity(registerReq)
                )
        );
        LOGGER.info("User " + userName + " has been registered");
        return true;
    }

    @PostConstruct
    public void init() {
        UserEntity admin = new UserEntity();
        admin.setId(1);
        admin.setEmail("admin@admin.ru");
        admin.setPassword(encoder.encode("password"));
        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setPhone("+71111111111");
        admin.setRole(Role.ADMIN);
        if (!manager.userExists(admin.getEmail())) {
            manager.createUser(new AppUser(admin));
        }
    }
}
