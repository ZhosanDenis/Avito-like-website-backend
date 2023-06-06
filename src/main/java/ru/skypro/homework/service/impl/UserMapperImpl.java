package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.account.RegisterReq;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.service.UserMapper;

import java.util.Objects;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity toUserEntity(RegisterReq req) {
        UserEntity user = new UserEntity();
        user.setEmail(req.getUsername());
        user.setPassword(Objects.hash(req.getUsername(), req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPhone(req.getPhone());
        user.setRole(req.getRole());
        return user;
    }
}
