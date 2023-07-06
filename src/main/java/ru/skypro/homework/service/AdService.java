package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateAds;
import ru.skypro.homework.dto.ads.FullAds;
import ru.skypro.homework.dto.ads.ResponseWrapperAds;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.CreateComment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AdService {
    ResponseWrapperComment getComments(Integer id);

    Comment addComment(Integer id, CreateComment createComment);

    boolean deleteComment(Integer adId, Integer commentId);

    Comment updateComment(Integer adId, Integer commentId, CreateComment createComment);

    Ads addAdvertising(CreateAds createAds, MultipartFile image) throws IOException;

    FullAds getAdvertising(int id);

    ResponseWrapperAds getAllAdvertising();

    ResponseWrapperAds getAllMyAdvertising();

    Ads updateAdvertising(int id, CreateAds createAds);

    boolean updateAdvertisingImage(int id, MultipartFile image) throws IOException;

    boolean deleteAdvertising(int id) throws IOException;

    ResponseWrapperAds findByTitle(String title);

    void downloadAdImageFromFS(int adId, HttpServletResponse response) throws IOException;
}
