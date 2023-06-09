package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.ResponseWrapperCommentMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdServiceImpl implements AdService {

    private final CommentRepository commentRepository;
    private final ResponseWrapperCommentMapper responseWrapperCommentMapper;

    public AdServiceImpl(CommentRepository commentRepository,
                         ResponseWrapperCommentMapper responseWrapperCommentMapper) {
        this.commentRepository = commentRepository;
        this.responseWrapperCommentMapper = responseWrapperCommentMapper;
    }

    @Override
    public ResponseWrapperComment getComments(Integer id) {

        ResponseWrapperComment responseWrapperComment = new ResponseWrapperComment();
        responseWrapperComment.setCount(commentRepository
                .countCommentEntitiesByAdEntity_Id(id));
        List<CommentEntity> commentEntities = commentRepository
                .findCommentEntitiesByAdEntity_Id(id);
        List<Comment> comments = new ArrayList<>();
        for (CommentEntity commentEntity : commentEntities) {
            comments.add(responseWrapperCommentMapper.toCommentDto(commentEntity));
        }
        responseWrapperComment.setResults(comments);
        return new ResponseWrapperComment();
    }

    @Override
    public Comment addComment(Integer id, Comment comment) {

        commentRepository.save(responseWrapperCommentMapper
                .toCommentEntity(id, comment));
        return comment;
    }

    @Override
    public void deleteComment(Integer adId, Integer commentId) {

        commentRepository.delete(commentRepository
                .findCommentEntityByIdAndAdEntity_Id(commentId, adId));
    }

    @Override
    public Comment updateComment(long adId, long commentId, Comment comment) {
        return null;
    }
}
