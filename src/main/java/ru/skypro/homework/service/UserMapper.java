package ru.skypro.homework.service;

import ru.skypro.homework.dto.account.RegisterReq;
import ru.skypro.homework.model.UserEntity;

public interface UserMapper {
    UserEntity toUserEntity(RegisterReq req);
}
