package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.CreateComment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.service.ResponseWrapperCommentMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResponseWrapperCommentMapperImpl implements ResponseWrapperCommentMapper {

    @Override
    public CommentEntity toCommentEntity(CreateComment createComment) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setText(createComment.getText());
        commentEntity.setCreatedAt(LocalDateTime.now());
        return commentEntity;
    }

    @Override
    public ResponseWrapperComment toResponseWrapperCommentDto(
            List<CommentEntity> commentEntityList) {
        ResponseWrapperComment responseWrapperComment = new ResponseWrapperComment();
        responseWrapperComment.setCount(commentEntityList.size());
        responseWrapperComment.setResults(commentEntityList.stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList()));
        return responseWrapperComment;
    }

    @Override
    public Comment toCommentDto(CommentEntity commentEntity) {
        Comment comment = new Comment();
        comment.setAuthor(commentEntity.getUserEntity().getId());
        comment.setAuthorImage("/users/image/" + commentEntity.getUserEntity().getId() + "/download");
        comment.setAuthorFirstName(commentEntity.getUserEntity().getFirstName());
        comment.setCreatedAt(commentEntity.getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
        comment.setPk(commentEntity.getId());
        comment.setText(commentEntity.getText());
        return comment;
    }

    @Override
    public CommentEntity toCommentEntity(Comment comment, CommentEntity commentEntity) {
        commentEntity.setId(comment.getPk());
        commentEntity.setText(comment.getText());
        commentEntity.setCreatedAt(Instant.ofEpochMilli(comment.getCreatedAt())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        UserEntity user = commentEntity.getUserEntity();
        user.setId(comment.getAuthor());
        user.setFirstName(comment.getAuthorFirstName());
        commentEntity.setUserEntity(user);
        return commentEntity;
    }
}
