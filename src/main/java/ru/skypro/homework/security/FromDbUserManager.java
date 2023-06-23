package ru.skypro.homework.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;

public class FromDbUserManager implements UserDetailsManager {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return new AppUser(userEntity);
    }

    @Override
    public void createUser(UserDetails user) {
        userRepository.save(((AppUser) user).getUser());
    }

    @Override
    public void updateUser(UserDetails user) {
        userRepository.save(((AppUser) user).getUser());
    }

    @Override
    public void deleteUser(String username) {
        userRepository.deleteByEmail(username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        String userName = currentUser.getName();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (encoder.matches(oldPassword, userEntity.getPassword())) {
            userEntity.setPassword(encoder.encode(newPassword));
            userRepository.save(userEntity);
        }
    }

    @Override
    public boolean userExists(String userName) {
        return userRepository.existsByEmail(userName);
    }
}
