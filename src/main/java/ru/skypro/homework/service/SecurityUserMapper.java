package ru.skypro.homework.service;

import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.security.UserDto;

public interface SecurityUserMapper {

    UserDto toUserDto(UserEntity userEntity);
}
