package ru.skypro.homework.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateAds;
import ru.skypro.homework.dto.ads.FullAds;
import ru.skypro.homework.dto.ads.ResponseWrapperAds;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.CreateComment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AdMapper;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.ResponseWrapperCommentMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
@EnableMethodSecurity(securedEnabled = true)
public class AdServiceImpl implements AdService {
    private final CommentRepository commentRepository;
    private final ResponseWrapperCommentMapper responseWrapperCommentMapper;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdMapper adMapper;

    @Value("${ads.image.dir.path}")
    private String adsImageDir;

    public AdServiceImpl(CommentRepository commentRepository,
                         ResponseWrapperCommentMapper responseWrapperCommentMapper,
                         UserRepository userRepository,
                         AdRepository adRepository,
                         AdMapper adMapper) {
        this.commentRepository = commentRepository;
        this.responseWrapperCommentMapper = responseWrapperCommentMapper;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.adMapper = adMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseWrapperComment getComments(Integer id) {
        List<CommentEntity> commentEntities = commentRepository
                .findCommentEntitiesByAdEntity_Id(id);
        return responseWrapperCommentMapper.toResponseWrapperCommentDto(commentEntities);
    }

    @Override
    @Transactional
    public Comment addComment(Integer id, CreateComment createComment, String userName) {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        CommentEntity commentEntity = responseWrapperCommentMapper.toCommentEntity(createComment, new CommentEntity());
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        commentEntity.setUserEntity(userEntity);
        commentEntity.setAdEntity(adEntity);
        return responseWrapperCommentMapper.toCommentDto(commentRepository.save(commentEntity));
    }

    @Override
    @Transactional
    public void deleteComment(Integer adId, Integer commentId) {
        commentRepository.delete(commentRepository
                .findByIdAndAdEntity_Id(commentId, adId));
    }

    @Override
    @Transactional
    public Comment updateComment(Integer adId, Integer commentId, CreateComment createComment) {
        CommentEntity commentEntity = commentRepository.findByIdAndAdEntity_Id(commentId, adId);
        return responseWrapperCommentMapper.toCommentDto(
                commentRepository.save(
                        responseWrapperCommentMapper.toCommentEntity(
                                createComment, commentEntity)
                )
        );
    }

    @Override
    @Transactional
    public Ads addAdvertising(CreateAds createAds, MultipartFile image, String userName) throws IOException {
        UserEntity user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        AdEntity adEntity = adRepository.save(adMapper.toAdEntity(createAds, new AdEntity()));
        Path filePath = createPath(image, adEntity);
        adEntity.setUserEntity(user);
        adEntity.setImagePath(filePath.getParent().toString());
        adEntity.setImageMediaType(image.getContentType());
        adEntity.setImageFileSize(image.getSize());

        return adMapper.toAds(adRepository.save(adEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public FullAds getAdvertising(int id) {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        return adMapper.toFullAds(adEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseWrapperAds getAllAdvertising() {
        List<AdEntity> entityList = adRepository.findAll();
        return adMapper.toResponseWrapperAds(entityList);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseWrapperAds getAllMyAdvertising(String userName) {
        List<AdEntity> entityList = adRepository.findAllByUserEntityEmail(userName);
        return adMapper.toResponseWrapperAds(entityList);
    }

    @Override
    @Transactional
    @Secured({"USER", "ADMIN"})
    public Ads updateAdvertising(int id, CreateAds createAds) {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        return adMapper.toAds(
                adRepository.save(
                        adMapper.toAdEntity(createAds, adEntity)
                )
        );
    }

    @Override
    @Transactional
    public boolean updateAdvertisingImage(int id, MultipartFile image) throws IOException {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        Path filePath = createPath(image, adEntity);
        adEntity.setImagePath(filePath.getParent().toString());
        adEntity.setImageMediaType(image.getContentType());
        adEntity.setImageFileSize(image.getSize());
        adRepository.save(adEntity);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteAdvertising(int id) {
        commentRepository.deleteAllByAdEntity_Id(id);
        adRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseWrapperAds findByTitle(String title) {
        return adMapper.toResponseWrapperAds(
                adRepository.findAllByTitleLike(title));
    }

    @Override
    public void downloadAdImageFromFS(int adId, HttpServletResponse response) throws IOException {
        AdEntity adEntity = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));

        AccountServiceImpl.findAndDownloadImage(response,
                adEntity.getImagePath(),
                adEntity.getImageMediaType(),
                adEntity.getImageFileSize());
    }

    private Path createPath(MultipartFile image, AdEntity adEntity) throws IOException {
        Path filePath = Path.of(adsImageDir, "Объявление_" + adEntity.getId() + "."
                + StringUtils.getFilenameExtension(image.getOriginalFilename()));
        AccountServiceImpl.uploadImage(image, filePath);
        return filePath;
    }
}
