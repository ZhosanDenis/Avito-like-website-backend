package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.CommentEntity;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    List<CommentEntity> findCommentEntitiesByAdEntity_Id(Integer id);

    CommentEntity findByIdAndAdEntity_Id(int commentId, int adId);

    void deleteAllByAdEntity_Id(int adId);
}
