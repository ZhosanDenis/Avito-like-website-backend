package ru.skypro.homework.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.SecurityUserMapper;

@Service
public class FromDbUserManager implements UserDetailsManager {

    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final SecurityUserMapper mapper;

    private final AppUser userDetails;

    public FromDbUserManager(UserRepository userRepository,
                             PasswordEncoder encoder,
                             SecurityUserMapper mapper,
                             AppUser userDetails) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.mapper = mapper;
        this.userDetails = userDetails;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto userDto = userRepository.findByEmail(username)
                .map(mapper::toUserDto)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        userDetails.setUser(userDto);
        return userDetails;
    }

    @Override
    public void createUser(UserDetails user) {
    }

    @Override
    public void updateUser(UserDetails user) {
    }

    @Override
    public void deleteUser(String username) {
        userRepository.deleteByEmail(username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        if (encoder.matches(oldPassword, userDetails.getPassword())) {
            UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
            userEntity.setPassword(encoder.encode(newPassword));
            userRepository.save(userEntity);
        }
    }

    @Override
    public boolean userExists(String userName) {
        return userRepository.existsByEmail(userName);
    }
}
