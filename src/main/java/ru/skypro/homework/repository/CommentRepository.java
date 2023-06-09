package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    List<CommentEntity> findCommentEntitiesByAdEntity_Id(Integer id);

    int countCommentEntitiesByAdEntity_Id(Integer id);

    CommentEntity findCommentEntityByIdAndAdEntity_Id(Integer id, Integer id2);

}
