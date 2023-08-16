package ru.skypro.homework.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.account.NewPassword;
import ru.skypro.homework.dto.account.User;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AccountService {

    /**
     * Обновление пароля пользователя
     * @param newPassword объект, содержащий текущий и новый пароли пользователя
     * @return возвращает {@code true}, если пароль изменён, или {@code false}, если новый пароль некорректен
     */
    boolean updatePassword(NewPassword newPassword);

    /**
     * Получение данных пользователя
     * @return пользователь
     * @throws UsernameNotFoundException если пользователь с таким логином не найден в БД
     */
    User getUserInfo();

    /**
     * Обновление пользователя
     * @param user пользователь
     * @return обновлённый пользователь
     * @throws UsernameNotFoundException если пользователь с таким логином не найден в БД
     */
    User patchUserInfo(User user);

    /**
     * Обновление аватарки пользователя
     * @param image файл картинки
     * @return {@code true}, если аватарка обновлёна
     * @throws IOException ошибка ввода-вывода
     * @throws UsernameNotFoundException если текущий пользователь не найден в БД
     */
    boolean updateUserAvatar(MultipartFile image) throws IOException;

    /**
     * Загрузка аватарки пользователя из файловой системы по его id
     * @param userId id пользователя
     * @param response ответ сервера
     * @return {@code true}, если аватар пользователя загружен
     * @throws IOException ошибка ввода - вывода
     */
    boolean downloadAvatarFromFS(int userId, HttpServletResponse response) throws IOException;
}
