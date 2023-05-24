package ru.skypro.homework.dto.account;

import lombok.Data;

@Data
public class LoginReq {
    private String password;
    private String username;
}
