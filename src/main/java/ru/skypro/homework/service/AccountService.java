package ru.skypro.homework.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.account.NewPassword;
import ru.skypro.homework.dto.account.User;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AccountService {
    void updatePassword(NewPassword newPassword, String userName);

    User getUserInfo(Authentication authentication);

    User patchUserInfo(User user, Authentication authentication);

    void updateUserAvatar(String userName, MultipartFile image) throws IOException;

    void downloadAvatarFromFS(int userId, HttpServletResponse response) throws IOException;
}
