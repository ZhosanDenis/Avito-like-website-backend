package ru.skypro.homework.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.security.AppUser;
import ru.skypro.homework.dto.account.RegisterReq;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.SecurityUserMapper;
import ru.skypro.homework.service.UserMapper;

import javax.annotation.PostConstruct;

@Service
public class AuthServiceImpl implements AuthService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserDetailsManager manager;

    private final PasswordEncoder encoder;

    private final UserMapper mapper;

    private final SecurityUserMapper securityUserMapper;

    private final UserRepository userRepository;

    public AuthServiceImpl(UserDetailsManager manager,
                           PasswordEncoder passwordEncoder,
                           UserMapper mapper, SecurityUserMapper securityUserMapper,
                           UserRepository userRepository) {
        this.manager = manager;
        this.encoder = passwordEncoder;
        this.mapper = mapper;
        this.securityUserMapper = securityUserMapper;
        this.userRepository = userRepository;
    }

    @Override
    public boolean login(String userName, String password) {
        if (!manager.userExists(userName)) {
            return false;
        }
        UserDetails userDetails = manager.loadUserByUsername(userName);
        return encoder.matches(password, userDetails.getPassword());
    }

    @Override
    public boolean register(RegisterReq registerReq, Role role) {
        String userName = registerReq.getUsername();
        if (manager.userExists(userName)) {
            return false;
        }
        manager.createUser(new AppUser(
                        securityUserMapper.toUserDto(
                                userRepository.save(
                                        mapper.toUserEntity(registerReq))
                        )
                )
        );
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
            LOGGER.info("Admin was created");
            manager.createUser(new AppUser(
                            securityUserMapper.toUserDto(admin)
                    )
            );
        }
    }
}
