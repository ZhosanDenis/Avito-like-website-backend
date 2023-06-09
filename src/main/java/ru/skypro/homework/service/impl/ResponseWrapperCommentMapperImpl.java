package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ResponseWrapperCommentMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class ResponseWrapperCommentMapperImpl implements ResponseWrapperCommentMapper {

    private final UserRepository userRepository;
    private final AdRepository adRepository;

    public ResponseWrapperCommentMapperImpl(UserRepository userRepository,
                                            AdRepository adRepository) {
        this.userRepository = userRepository;
        this.adRepository = adRepository;
    }


    @Override
    public ResponseWrapperComment toResponseWrapperCommentDto(
            List<Comment> comments) {

        ResponseWrapperComment responseWrapperComment = new ResponseWrapperComment();
        responseWrapperComment.setCount(0);
        responseWrapperComment.setResults(comments);
        return responseWrapperComment;
    }

    @Override
    public Comment toCommentDto(CommentEntity commentEntity) {

        Comment comment = new Comment();
        comment.setAuthor(commentEntity.getUserEntity().getId());
        comment.setAuthorImage(commentEntity.getUserEntity().getImage());
        comment.setAuthorFirstName(commentEntity.getUserEntity().getFirstName());
        comment.setCreatedAt(commentEntity.getCreatedAt()
                .toInstant(ZoneOffset.UTC).toEpochMilli());
        comment.setPk(commentEntity.getId());
        comment.setText(commentEntity.getText());
        return comment;
    }

    @Override
    public CommentEntity toCommentEntity(Integer id, Comment comment) {

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setId(id);
        commentEntity.setText(comment.getText());
        commentEntity.setCreatedAt(LocalDateTime.now());
        UserEntity userEntity = userRepository.findById(comment.getAuthor())
                .orElseThrow(IllegalArgumentException::new);
        commentEntity.setUserEntity(userEntity);
        AdEntity adEntity = adRepository.findById(comment.getPk())
                .orElseThrow(IllegalArgumentException::new);
        commentEntity.setAdEntity(adEntity);
        return commentEntity;
    }

}
