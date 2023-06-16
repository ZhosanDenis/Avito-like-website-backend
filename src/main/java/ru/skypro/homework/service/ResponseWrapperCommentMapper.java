package ru.skypro.homework.service;

import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.CreateComment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;
import ru.skypro.homework.model.CommentEntity;

import java.util.List;

public interface ResponseWrapperCommentMapper {

    CommentEntity toCommentEntity(CreateComment createComment, CommentEntity commentEntity);

    ResponseWrapperComment toResponseWrapperCommentDto(List<CommentEntity> comments);

    Comment toCommentDto(CommentEntity commentEntity);
}
