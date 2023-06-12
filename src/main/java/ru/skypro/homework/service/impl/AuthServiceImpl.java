package ru.skypro.homework.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.account.RegisterReq;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.UserMapper;

import java.util.Objects;

@Service
public class AuthServiceImpl implements AuthService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserDetailsManager manager;

    private final PasswordEncoder encoder;

    private final UserMapper mapper;

    private final UserRepository userRepository;

    public AuthServiceImpl(UserDetailsManager manager,
                           PasswordEncoder passwordEncoder,
                           UserMapper mapper,
                           UserRepository userRepository) {
        this.manager = manager;
        this.encoder = passwordEncoder;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @Override
    public boolean login(String userName, String password) {
        boolean passwordMatches = userRepository.existsByPassword(Objects.hash(userName, password));
        if (!manager.userExists(userName)) {
            LOGGER.warn("User does not exist");
            return false;
        }
//        UserDetails userDetails = manager.loadUserByUsername(userName);
        LOGGER.info("User " + userName + " logged in");
        return /*encoder.matches(password, userDetails.getPassword()) ||*/ passwordMatches;
    }

    @Override
    public boolean register(RegisterReq registerReq, Role role) {
        if (manager.userExists(registerReq.getUsername())) {
            return false;
        }
        manager.createUser(
                User.builder()
                        .passwordEncoder(this.encoder::encode)
                        .password(registerReq.getPassword())
                        .username(registerReq.getUsername())
                        .roles(role.name())
                        .build());
        userRepository.save(mapper.toUserEntity(registerReq));
        return true;
    }
}
