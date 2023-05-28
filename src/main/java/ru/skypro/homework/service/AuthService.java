package ru.skypro.homework.service;

import ru.skypro.homework.dto.account.RegisterReq;
import ru.skypro.homework.dto.account.Role;

public interface AuthService {
    boolean login(String userName, String password);
    boolean register(RegisterReq registerReq, Role role);
}
