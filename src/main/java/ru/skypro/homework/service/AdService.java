package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateAds;
import ru.skypro.homework.dto.ads.FullAds;
import ru.skypro.homework.dto.ads.ResponseWrapperAds;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.CreateComment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;

import java.io.IOException;

public interface AdService {
    ResponseWrapperComment getComments(Integer id);

    Comment addComment(Integer id, CreateComment createComment, String userName);

    void deleteComment(Integer adId, Integer commentId);

    Comment updateComment(Integer adId, Integer commentId, Comment comment);

    Ads addAdvertising(CreateAds createAds, MultipartFile image, String userName) throws IOException;

    FullAds getAdvertising(int id, String userName);

    ResponseWrapperAds getAllAdvertising();

    ResponseWrapperAds getAllMyAdvertising(String userName);

    Ads updateAdvertising(int id, CreateAds createAds, String userName);

    boolean updateAdvertisingImage(int id, MultipartFile image, String userName) throws IOException;

    boolean deleteAdvertising(int id, String userName);
}
