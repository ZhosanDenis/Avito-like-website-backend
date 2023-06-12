package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.account.RegisterReq;
import ru.skypro.homework.dto.account.User;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.service.UserMapper;

import java.util.Objects;

@Component
public class UserMapperImpl implements UserMapper {

    public UserEntity toUserEntity(RegisterReq req) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(req.getUsername());
        userEntity.setPassword(Objects.hash(req.getUsername(), req.getPassword()));
        userEntity.setFirstName(req.getFirstName());
        userEntity.setLastName(req.getLastName());
        userEntity.setPhone(req.getPhone());
        userEntity.setRole(req.getRole());
        return userEntity;
    }

    @Override
    public UserEntity updateUserEntity(UserEntity userEntity, User user) {
        userEntity.setEmail(user.getEmail());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setPhone(user.getPhone());
        return userEntity;
    }

    @Override
    public User toUser(UserEntity userEntity) {
        User user = new User();
        user.setId(userEntity.getId());
        user.setEmail(userEntity.getEmail());
        user.setFirstName(userEntity.getFirstName());
        user.setLastName(userEntity.getLastName());
        user.setPhone(userEntity.getPhone());
        user.setImage("/users/image/" + userEntity.getId() + "/download");
        return user;
    }
}
