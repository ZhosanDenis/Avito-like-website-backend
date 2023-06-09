package ru.skypro.homework.service;

import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;

public interface AdService {
    ResponseWrapperComment getComments(Integer id);

    Comment addComment(Integer id, Comment comment);

    void deleteComment(Integer adId, Integer commentId);

    Comment updateComment(long adId, long commentId, Comment comment);
}
