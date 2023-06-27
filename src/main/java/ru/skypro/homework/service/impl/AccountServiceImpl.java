package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.account.NewPassword;
import ru.skypro.homework.dto.account.User;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AccountService;
import ru.skypro.homework.service.UserMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserDetailsManager manager;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserDetails userDetails;

    @Value("${users.avatar.dir.path}")
    private String avatarsDir;

    @Override
    @Transactional
    public boolean updatePassword(NewPassword newPassword) {
        if (userDetails != null) {
            manager.changePassword(newPassword.getCurrentPassword(), newPassword.getNewPassword());
            log.info("Password was changed for user " + userDetails.getUsername());
            return true;
        }
        log.warn("User does not exist");
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserInfo() {
        String userName = userDetails.getUsername();
        log.info("Information was received for user " + userName);
        return userMapper.toUser(
                (userRepository.findByEmail(userName))
                        .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"))
        );
    }

    @Override
    @Transactional
    public User patchUserInfo(User user) {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        UserEntity updatedUser = userRepository.save(userMapper.updateUserEntity(userEntity, user));
        log.info("Information was updated for user " + userName);
        return userMapper.toUser(updatedUser);
    }

    @Override
    @Transactional
    public boolean updateUserAvatar(MultipartFile image) throws IOException {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        Path filePath = Path.of(avatarsDir, userEntity.getId() + "."
                + StringUtils.getFilenameExtension(image.getOriginalFilename()));
        uploadImage(image, filePath);
        userEntity.setImagePath(filePath.getParent().toString());
        userEntity.setImageMediaType(image.getContentType());
        userEntity.setImageFileSize(image.getSize());
        log.info("Avatar was updated for user " + userName);
        return true;
    }

    @Override
    @Transactional
    public void downloadAvatarFromFS(int userId, HttpServletResponse response) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        findAndDownloadImage(response,
                user.getImagePath(),
                user.getImageMediaType(),
                user.getImageFileSize());
        log.info("The method was called to download avatar for user " + user.getEmail());
    }

    static void findAndDownloadImage(HttpServletResponse response,
                                     String imagePath,
                                     String imageMediaType,
                                     long imageFileSize) throws IOException {
        Path path = Path.of(imagePath);

        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream()) {
            response.setStatus(200);
            response.setContentType(imageMediaType);
            response.setContentLength((int) imageFileSize);
            is.transferTo(os);
        }
    }

    static void uploadImage(MultipartFile image, Path filePath) throws IOException {
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);

        try (InputStream is = image.getInputStream();
             OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
             BufferedInputStream bis = new BufferedInputStream(is, 1024);
             BufferedOutputStream bos = new BufferedOutputStream(os, 1024)
        ) {
            bis.transferTo(bos);
        }
    }
}
