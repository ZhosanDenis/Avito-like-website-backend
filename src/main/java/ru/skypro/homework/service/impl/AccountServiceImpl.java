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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

/**
 * Класс предназначен для осуществления операций с БД пользователей
 */
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

    /**
     * Обновление пароля текущего пользователя. На вход передаётся объект класса
     * <b>NewPassword</b>,
     * происходит проверка корректности нового пароля,
     * сохраняется новый пароль для текущего пользователя.
     * Используется метод класса {@link UserDetailsManager#changePassword(String, String)}
     * Используются методы класса {@link NewPassword#getNewPassword()}
     * Используются методы класса {@link NewPassword#getCurrentPassword()}
     * @param newPassword объект, содержащий текущий и новый пароли пользователя
     * @return возвращает {@code true}, если пароль изменён, или {@code false}, если новый пароль некорректен
     * @see UserDetails#getUsername()
     */
    @Override
    @Transactional
    public boolean updatePassword(NewPassword newPassword) {
        if (newPassword != null &&
                newPassword.getNewPassword() != null &&
                !newPassword.getNewPassword().isEmpty() &&
                !newPassword.getNewPassword().isBlank()) {
            manager.changePassword(newPassword.getCurrentPassword(), newPassword.getNewPassword());
            log.info("Password was changed for user " + userDetails.getUsername());
            return true;
        }
        log.warn("New password for user " + userDetails.getUsername() + " is incorrect");
        return false;
    }


    /**
     * Получение текущего пользователя из БД путём
     * получения имени пользователя (логина) из объекта класса <b>UserDetails</b>.
     * <br>Используется метод {@link UserDetails#getUsername()}
     * <br>Для получения пользователя используется метод класса
     * {@link UserRepository#findByEmail(String)}
     * @return Пользователь
     * @throws UsernameNotFoundException если пользователь с таким логином не найден в БД
     */
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

    /**
     * Обновление данных текущего пользователя. Логин пользователя получается из объекта
     * класса <b>UserDetails</b> методом {@link UserDetails#getUsername()}
     * <br> Получение пользователя из БД происходит в методе
     * {@link UserRepository#findByEmail(String)}
     * <br> Сохранение пользователя происходит в методе {@link UserRepository#save(Object)}
     * @param user Данные пользователя из веб-интерфейса
     * @return обновлённый пользователь
     * @throws UsernameNotFoundException если пользователь с таким логином не найден в БД
     * @see UserMapper#updateUserEntity(UserEntity, User)
     */
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

    /**
     * Обновление аватара текущего пользователя. Используется метод
     * <br> <b>uploadImage(image, filePath)</b>. Логин пользователя получается из объекта
     * класса <b>UserDetails</b> методом {@link UserDetails#getUsername()}
     * <br> Получение пользователя из БД происходит в методе
     * {@link UserRepository#findByEmail(String)}
     * @param image файл картинки
     * @return {@code true}, если аватар обновлён
     * @throws IOException ошибка ввода-вывода
     * @throws UsernameNotFoundException если текущий пользователь не найден в БД
     * @see Files#deleteIfExists(Path)
     * @see UserEntity#getImagePath()
     * @see Path#of(String, String...)
     * @see StringUtils#getFilenameExtension(String)
     */
    @Override
    @Transactional
    public boolean updateUserAvatar(MultipartFile image) throws IOException {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        if (userEntity.getImagePath() != null) {
            Files.deleteIfExists(Path.of(userEntity.getImagePath()));
        }
        Path filePath = Path.of(avatarsDir, userEntity.getId() + "."
                + StringUtils.getFilenameExtension(image.getOriginalFilename()));
        uploadImage(image, filePath);
        userEntity.setImagePath(filePath.toAbsolutePath().toString());
        userEntity.setImageMediaType(image.getContentType());
        userEntity.setImageFileSize(image.getSize());
        userRepository.save(userEntity);
        log.info("Avatar was updated for user " + userName);
        return true;
    }

    /**
     * Загрузка аватара из файловой системы по id пользователя. <br> Используется метод
     * {@link UserRepository#findById(Object)} для получения пользователя из БД.
     * Для формирования ответа сервера используется метод
     * {@link #findAndDownloadImage(HttpServletResponse, String, String, long)}
     * @param userId id пользователя
     * @param response ответ сервера
     * @return {@code true}, если аватар пользователя загружен
     * @throws IOException ошибка ввода-вывода
     * @throws UsernameNotFoundException если пользователь с данным id не найден в БД
     * @see #downloadAvatarFromFS(int, HttpServletResponse)
     */
    @Override
    @Transactional
    public boolean downloadAvatarFromFS(int userId, HttpServletResponse response)
            throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (user.getImagePath() != null) {
            findAndDownloadImage(response,
                    user.getImagePath(),
                    user.getImageMediaType(),
                    user.getImageFileSize());
            log.info("The method was called to download avatar for user " + user.getEmail());
            return true;
        }
        return false;
    }

    /**
     * Копирование данных из файла рисунка в ответе сервера. Входной поток получаем
     * из метода {@link Files#newInputStream(Path, OpenOption...)}. Выходной поток
     * получаем из метода {@link HttpServletResponse#getOutputStream()}
     * @param response ответ сервера
     * @param imagePath путь и название файла с аватаркой
     * @param imageMediaType тип файла аватарки
     * @param imageFileSize размер файла аватарки
     * @throws IOException ошибка ввода - вывода
     * @see Path#of(URI)
     */
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

    /**
     * Загрузка на сервер файла картинки. Входной поток получаем методом
     * {@link MultipartFile#getInputStream()}. Выходной поток получаем методом
     * {@link Files#newOutputStream(Path, OpenOption...)}
     * @param image файл картинки
     * @param filePath путь к файлу на сервере
     * @throws IOException ошибка ввода - вывода
     * @see Files#createDirectories(Path, FileAttribute[])
     * @see Files#deleteIfExists(Path)
     */
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
