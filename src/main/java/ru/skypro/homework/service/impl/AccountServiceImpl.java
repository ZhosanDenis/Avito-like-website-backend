package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder encoder;

    @Value("${users.avatar.dir.path}")
    private String avatarsDir;

    @Override
    @Transactional
    public boolean updatePassword(NewPassword newPassword, String userName) {
        UserEntity user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (encoder.matches(newPassword.getCurrentPassword(), user.getPassword())) {
            user.setPassword(encoder.encode(newPassword.getNewPassword()));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserInfo(String userName) {
        return userMapper.toUser(
                userRepository.findByEmail(userName)
                        .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"))
        );
    }

    @Override
    @Transactional
    public User patchUserInfo(User user, String userName) {
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return userMapper.toUser(
                userRepository.save(
                        userMapper.updateUserEntity(userEntity, user)
                )
        );
    }

    @Override
    @Transactional
    public boolean updateUserAvatar(String userName, MultipartFile image) throws IOException {
        UserEntity user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Path filePath = Path.of(avatarsDir, user.getId() + "."
                + StringUtils.getFilenameExtension(image.getOriginalFilename()));
        uploadImage(image, filePath);
        user.setImagePath(filePath.getParent().toString());
        user.setImageMediaType(image.getContentType());
        user.setImageFileSize(image.getSize());
        userRepository.save(user);
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
