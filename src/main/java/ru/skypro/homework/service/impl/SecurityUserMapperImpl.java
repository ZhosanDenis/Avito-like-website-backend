package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Component;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.security.UserDto;
import ru.skypro.homework.service.SecurityUserMapper;

@Component
public class SecurityUserMapperImpl implements SecurityUserMapper {
    @Override
    public UserDto toUserDto(UserEntity userEntity) {
        UserDto userDto = new UserDto();
        userDto.setId(userEntity.getId());
        userDto.setUserName(userEntity.getEmail());
        userDto.setPassword(userEntity.getPassword());
        userDto.setRole(userEntity.getRole());
        return userDto;
    }
}
