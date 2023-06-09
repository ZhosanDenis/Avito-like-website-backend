package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.account.NewPassword;
import ru.skypro.homework.dto.account.User;
import ru.skypro.homework.service.AccountService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("users")
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "Обновление пароля",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Пользователи"
    )
    @PostMapping("/set_password")
    public ResponseEntity<?> updatePassword(@RequestBody NewPassword newPassword, Authentication authentication) {
        String userName = authentication.getName();
        accountService.updatePassword(newPassword, userName);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Получить информацию об авторизованном пользователе",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = User.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Пользователи"
    )
    @GetMapping("/me")
    public ResponseEntity<User> getUserInfo(Authentication authentication) {
        return ResponseEntity.ok(accountService.getUserInfo(authentication));
    }

    @Operation(
            summary = "Обновить информацию об авторизованном пользователе",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = User.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Пользователи"
    )
    @PatchMapping("/me")
    public ResponseEntity<User> patchUserInfo(@RequestBody User user, Authentication authentication) {
        return ResponseEntity.ok(accountService.patchUserInfo(user, authentication));
    }

    @Operation(
            summary = "Обновить аватар авторизованного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Пользователи"
    )
    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUserAvatar(@RequestParam MultipartFile image, Authentication authentication) throws IOException {
        String userName = authentication.getName();
        if (image.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("File is too big");
        }
        accountService.updateUserAvatar(userName, image);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/image/{userId}/download")
    public void downloadUserAvatarFromFS(@PathVariable int userId, HttpServletResponse response) throws IOException {
        accountService.downloadAvatarFromFS(userId, response);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
