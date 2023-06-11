package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.CommentEntity;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    List<CommentEntity> findCommentEntitiesByAdEntity_Id(Integer id);

    CommentEntity findCommentEntityByIdAndAdEntity_Id(Integer commentId, Integer adId);

    CommentEntity findByIdAndAdEntity_Id(int adId, int commentId);
}
