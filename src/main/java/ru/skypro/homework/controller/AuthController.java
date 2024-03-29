package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.dto.account.LoginReq;
import ru.skypro.homework.dto.account.RegisterReq;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.service.AuthService;

import static ru.skypro.homework.dto.account.Role.USER;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Авторизация пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Авторизация"
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        String userName = req.getUsername();
        if (authService.login(userName, req.getPassword())) {
            log.info("User " + userName + " logged in");
            return ResponseEntity.ok().build();
        } else {
            log.warn("User " + userName + " does not exist");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(
            summary = "Регистрация пользователя",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Регистрация"
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq req) {
        Role role = (req.getRole() == null) ? USER : req.getRole();
        req.setRole(role);
        String userName = req.getUsername();
        if (authService.register(req, role)) {
            log.info("User " + userName + " has been registered");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            log.warn("User can not register, because user with email " + userName + " exists in the database");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
