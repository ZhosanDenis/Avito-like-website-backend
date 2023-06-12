package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.account.NewPassword;
import ru.skypro.homework.dto.account.User;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AccountService;
import ru.skypro.homework.service.UserMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Value("${users.avatar.dir.path}")
    private String avatarsDir;

    @Override
    public boolean updatePassword(NewPassword newPassword, String userName) {
        if (userRepository.existsByEmail(userName) &&
                userRepository.existsByPassword(Objects.hash(userName, newPassword.getCurrentPassword()))) {
            UserEntity user = userRepository.findByEmail(userName);
            user.setPassword(Objects.hash(userName, newPassword.getNewPassword()));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public User getUserInfo(Authentication authentication) {
        return userMapper.toUser(
                userRepository.findByEmail(authentication.getName())
        );
    }

    @Override
    public User patchUserInfo(User user, Authentication authentication) {
        UserEntity userEntity = userRepository.findByEmail(authentication.getName());
        return userMapper.toUser(
                userRepository.save(
                        userMapper.updateUserEntity(userEntity, user)
                )
        );
    }

    @Override
    public boolean updateUserAvatar(String userName, MultipartFile image) throws IOException {
        if (userRepository.existsByEmail(userName)) {
            UserEntity user = userRepository.findByEmail(userName);
            Path filePath = Path.of(avatarsDir, user.getId() + "."
                    + StringUtils.getFilenameExtension(image.getOriginalFilename()));
            uploadImage(image, filePath);
            user.setImagePath(filePath.getParent().toString());
            user.setImageMediaType(image.getContentType());
            user.setImageFileSize(image.getSize());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void downloadAvatarFromFS(int userId, HttpServletResponse response) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Path path = Path.of(user.getImagePath());

        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream()) {
            response.setStatus(200);
            response.setContentType(user.getImageMediaType());
            response.setContentLength((int) user.getImageFileSize());
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
