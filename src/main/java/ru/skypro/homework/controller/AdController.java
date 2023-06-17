package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateAds;
import ru.skypro.homework.dto.ads.FullAds;
import ru.skypro.homework.dto.ads.ResponseWrapperAds;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.CreateComment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;
import ru.skypro.homework.service.AdService;

import java.io.IOException;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("ads")
public class AdController {

    private final AdService adService;

    @Operation(
            summary = "Добавить объявление",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Created",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Ads.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Объявления"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ads> addAdvertising(@RequestPart CreateAds properties,
                                              @RequestPart MultipartFile image,
                                              Authentication authentication) throws IOException {
        String userName = authentication.getName();
        return ResponseEntity.ok(adService.addAdvertising(properties, image, userName));
    }

    @Operation(
            summary = "Получить информацию об объявлении",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = FullAds.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Объявления"
    )
    @GetMapping("/{id}")
    public ResponseEntity<FullAds> getAdvertising(@PathVariable int id) {
        return ResponseEntity.ok(adService.getAdvertising(id));
    }

    @Operation(
            summary = "Получить все объявления",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapperAds.class)
                            ))
            },
            tags = "Объявления"
    )
    @GetMapping
    public ResponseEntity<ResponseWrapperAds> getAllAdvertising() {
        return ResponseEntity.ok(adService.getAllAdvertising());
    }

    @Operation(
            summary = "Получить объявления авторизованного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapperAds.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Объявления"
    )
    @GetMapping("/me")
    public ResponseEntity<ResponseWrapperAds> getAllMyAdvertising(Authentication authentication) {
        String userName = authentication.getName();
        return ResponseEntity.ok(adService.getAllMyAdvertising(userName));
    }

    @Operation(
            summary = "Обновить информацию об объявлении",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Ads.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Объявления"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<Ads> updateAdvertising(@PathVariable int id,
                                                 @RequestBody CreateAds createAds) {
        return ResponseEntity.ok(adService.updateAdvertising(id, createAds));
    }

    @Operation(
            summary = "Обновить картинку объявления",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = String.class))
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Объявления"
    )
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAdvertisingImage(@PathVariable int id,
                                                    @RequestParam MultipartFile image) throws IOException {
        adService.updateAdvertisingImage(id, image);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "Удалить объявление",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Объявления"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdvertising(@PathVariable int id) {
        adService.deleteAdvertising(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Добавить комментарий к объявлению",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Comment.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Комментарии"
    )
    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable(name = "id") Integer id,
            @RequestBody CreateComment createComment,
            Authentication authentication) {
        String userName = authentication.getName();
        return ResponseEntity.ok(adService.addComment(id, createComment, userName));
    }

    @Operation(
            summary = "Получить комментарии объявления",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapperComment.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Комментарии"
    )
    @GetMapping("/{id}/comments")
    public ResponseEntity<ResponseWrapperComment> getComments(
            @PathVariable(name = "id") Integer id) {
        return ResponseEntity.ok(adService.getComments(id));
    }

    @Operation(
            summary = "Обновить комментарий",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Comment.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Комментарии"
    )
    @PatchMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable(name = "adId") Integer adId,
            @PathVariable(name = "commentId") Integer commentId,
            @RequestBody CreateComment createComment) {
        return ResponseEntity.ok(adService.updateComment(adId, commentId, createComment));
    }

    @Operation(
            summary = "Удалить комментарий",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Комментарии"
    )
    @DeleteMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable(name = "adId") Integer adId,
            @PathVariable(name = "commentId") Integer commentId) {
        adService.deleteComment(adId, commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Найти объявление по названию",
            tags = "Объявления"
    )
    @GetMapping("/find/{title}")
    public ResponseEntity<ResponseWrapperAds> findAdvertisingByTitle(@PathVariable String title) {
        return ResponseEntity.ok(adService.findByTitle(title));
    }
}
