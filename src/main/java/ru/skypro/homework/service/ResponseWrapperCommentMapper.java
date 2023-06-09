package ru.skypro.homework.service;

import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;

import java.util.ArrayList;
import java.util.List;

public interface ResponseWrapperCommentMapper {

    ResponseWrapperComment toResponseWrapperCommentDto(List<Comment> comments);

    Comment toCommentDto(CommentEntity commentEntity);

    CommentEntity toCommentEntity(Integer id, Comment comment);
}
