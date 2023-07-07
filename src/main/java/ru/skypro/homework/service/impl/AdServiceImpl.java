package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.account.Role;
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
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

/**
 * Класс предназначен для проведения операция с базами данных объявлений и комментариев
 */
@Slf4j
@Service
public class AdServiceImpl implements AdService {
    private final CommentRepository commentRepository;
    private final ResponseWrapperCommentMapper responseWrapperCommentMapper;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final UserDetails userDetails;

    @Value("${ads.image.dir.path}")
    private String adsImageDir;

    public AdServiceImpl(CommentRepository commentRepository,
                         ResponseWrapperCommentMapper responseWrapperCommentMapper,
                         UserRepository userRepository,
                         AdRepository adRepository,
                         AdMapper adMapper,
                         UserDetails userDetails) {
        this.commentRepository = commentRepository;
        this.responseWrapperCommentMapper = responseWrapperCommentMapper;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.adMapper = adMapper;
        this.userDetails = userDetails;
    }

    /**
     * Получение всех комментариев из БД по id объявления.<br>
     * - Поиск в БД всех комментариев по id объявления {@link CommentRepository#findCommentEntitiesByAdEntity_Id(Integer)}.<br>
     * - Преобразование (маппинг) списка найденных комментариев в объект возвращаемого класса {@link ResponseWrapperCommentMapper#toResponseWrapperCommentDto(List)}.
     * @param id идентификатор объявления в БД
     * @return объект {@link ResponseWrapperComment}, содержащий количество комментариев и список комментариев к данному объявлению
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseWrapperComment getComments(Integer id) {
        List<CommentEntity> commentEntities = commentRepository
                .findCommentEntitiesByAdEntity_Id(id);
        return responseWrapperCommentMapper.toResponseWrapperCommentDto(commentEntities);
    }

    /**
     * Создание в БД комментария к выбранному объявлению.<br>
     * - Поиск объявления в БД по id {@link AdRepository#findById(Object)}.<br>
     * - Создание комментария из входных данных {@link ResponseWrapperCommentMapper#toCommentEntity(CreateComment, CommentEntity)}.<br>
     * - Поиск пользователя в БД по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Задание найденных объявление и пользователей созданному комментарию {@link CommentEntity#setAdEntity(AdEntity)}, {@link CommentEntity#setUserEntity(UserEntity)}.<br>
     * - Сохранение созданного комментария в БД {@link CommentRepository#save(Object)}.<br>
     * - Преобразование (маппинг) созданного комментария в объект возвращаемого класса {@link ResponseWrapperCommentMapper#toCommentDto(CommentEntity)}.
     * @param id идентификатор объявления в БД
     * @param createComment объект, содержащий текст комментария
     * @return объект {@link Comment}, содержащий необходимую для пользователя информацию о созданном комментарии
     */
    @Override
    @Transactional
    public Comment addComment(Integer id, CreateComment createComment) {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        CommentEntity commentEntity = responseWrapperCommentMapper.toCommentEntity(createComment, new CommentEntity());
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        commentEntity.setUserEntity(userEntity);
        commentEntity.setAdEntity(adEntity);
        return responseWrapperCommentMapper.toCommentDto(commentRepository.save(commentEntity));
    }

    /**
     * Удаления из БД комментария выбранного объявления.<br>
     * - Поиск пользователя в БД по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Поиск комментария в БД по идентификатору комментария и идентификатору объявления {@link CommentRepository#findByIdAndAdEntity_Id(int, int)}.<br>
     * - Удаление комментария из БД {@link CommentRepository#delete(Object)}.
     * @param adId идентификатор объявления в БД
     * @param commentId идентификатор комментария в БД
     * @return <B>true</B>, если пользователь авторизован на удаление комментария и комментарий удален.<br>
     * В противном случае <B>false</B>
     */
    @Override
    @Transactional
    public boolean deleteComment(Integer adId, Integer commentId) {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        CommentEntity commentEntity = commentRepository.findByIdAndAdEntity_Id(commentId, adId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));
        if (userCanChangeComment(userEntity, commentEntity)) {
            commentRepository.delete(commentEntity);
            return true;
        }
        return false;
    }

    /**
     * Обновление комментария выбранного объявления.<br>
     * - Поиск пользователя в БД по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Поиск комментария в БД по идентификатору комментария и идентификатору объявления {@link CommentRepository#findByIdAndAdEntity_Id(int, int)}.<br>
     * - Преобразование (маппинг) найденного комментария и входных данных в обновленный комментарий {@link ResponseWrapperCommentMapper#toCommentEntity(CreateComment, CommentEntity)}.<br>
     * - Сохранение обновленного комментария в БД {@link CommentRepository#save(Object)}.<br>
     * - Преобразование (маппинг) обновленного комментария в объект возвращаемого класса {@link ResponseWrapperCommentMapper#toCommentDto(CommentEntity)}.
     * @param adId идентификатор объявления в БД
     * @param commentId идентификатор комментария в БД
     * @param createComment объект, содержащий текст комментария
     * @return объект {@link Comment}, содержащий необходимую для пользователя информацию об обновленном комментарии, если пользователь авторизован на редактирование комментария и комментарий обновлен.<br>
     * В противном случае <B>null</B>
     */
    @Override
    @Transactional
    public Comment updateComment(Integer adId, Integer commentId, CreateComment createComment) {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        CommentEntity commentEntity = commentRepository.findByIdAndAdEntity_Id(commentId, adId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));
        if (userCanChangeComment(userEntity, commentEntity)) {
            return responseWrapperCommentMapper.toCommentDto(
                    commentRepository.save(
                            responseWrapperCommentMapper.toCommentEntity(
                                    createComment, commentEntity)
                    )
            );
        }
        return null;
    }

    /**
     * Создание в БД объявления.<br>
     * - Поиск пользователя в БД по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Создание объявления из входных данных {@link AdMapper#toAdEntity(CreateAds, AdEntity)}, {@link AdRepository#save(Object)}.<br>
     * - Создание пути для загрузки изображения объявления {@link #createPath(MultipartFile, AdEntity)}.<br>
     * - Загрузка с сайта и сохранение в файловой системе изображения объявления {@link AccountServiceImpl#uploadImage(MultipartFile, Path)}.<br>
     * - Задание необходимых параметров созданному объявлению {@link AdEntity#setUserEntity(UserEntity)}, {@link AdEntity#setImagePath(String)}, {@link AdEntity#setImageMediaType(String)}, {@link AdEntity#setImageFileSize(long)}.<br>
     * - Сохранение в БД созданного объявления {@link AdRepository#save(Object)}.<br>
     * - Преобразование (маппинг) созданного объявления в объект возвращаемого класса {@link AdMapper#toAds(AdEntity)}.
     * @param createAds объект, содержащий необходимую информацию для создания объявления
     * @param image загружаемое изображение
     * @return объект {@link Ads}, содержащий необходимую для пользователя информацию о созданном объявлении
     * @throws IOException выбрасывается при ошибках, возникающих во время загрузки изображения
     */
    @Override
    @Transactional
    public Ads addAdvertising(CreateAds createAds, MultipartFile image) throws IOException {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        AdEntity adEntity = adRepository.save(adMapper.toAdEntity(createAds, new AdEntity()));
        Path filePath = createPath(image, adEntity);
        adEntity.setUserEntity(user);
        adEntity.setImagePath(filePath.toAbsolutePath().toString());
        adEntity.setImageMediaType(image.getContentType());
        adEntity.setImageFileSize(image.getSize());

        return adMapper.toAds(adRepository.save(adEntity));
    }

    /** Получение объявления по id.<br>
     * - Поиск объявления по id {@link AdRepository#findById(Object)}.<br>
     * - Преобразование (маппинг) найденного объявления в объект возвращаемого класса {@link AdMapper#toFullAds(AdEntity)}.
     * @param id идентификатор объявления в БД
     * @return объект {@link FullAds}, содержащий необходимую для пользователя информацию о запрашиваемом объявлении
     */
    @Override
    @Transactional(readOnly = true)
    public FullAds getAdvertising(int id) {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        return adMapper.toFullAds(adEntity);
    }

    /**
     * Получение всех объявлений.<br>
     * - Поиск в БД всех объявлений {@link AdRepository#findAll()}.<br>
     * - Преобразование (маппинг) списка найденных объявлений в объект возвращаемого класса {@link AdMapper#toResponseWrapperAds(List)}.
     * @return объект {@link ResponseWrapperAds}, содержащий количество объявлений и список объявлений
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseWrapperAds getAllAdvertising() {
        List<AdEntity> entityList = adRepository.findAll();
        return adMapper.toResponseWrapperAds(entityList);
    }

    /**
     * Получение всех объявлений аутентифицированного пользователя.<br>
     * - Поиск в БД всех объявлений текущего пользователя {@link AdRepository#findAllByUserEntityEmail(String)}.<br>
     * - Преобразование (маппинг) списка найденных объявлений в объект возвращаемого класса {@link AdMapper#toResponseWrapperAds(List)}.
     * @return объект {@link ResponseWrapperAds}, содержащий количество объявлений и список объявлений текущего пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseWrapperAds getAllMyAdvertising() {
        List<AdEntity> entityList = adRepository.findAllByUserEntityEmail(userDetails.getUsername());
        return adMapper.toResponseWrapperAds(entityList);
    }

    /**
     * Обновление объявления.<br>
     * - Поиск пользователя в БД по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Поиск объявления в БД по идентификатору объявления {@link AdRepository#findById(Object)}.<br>
     * - Преобразование (маппинг) найденного объявления и входных данных в обновленное объявление {@link AdMapper#toAdEntity(CreateAds, AdEntity)}.<br>
     * - Сохранение обновленного объявления в БД {@link AdRepository#save(Object)}.<br>
     * - Преобразование (маппинг) обновленного объявления в объект возвращаемого класса {@link AdMapper#toAds(AdEntity)}.
     * @param id идентификатор объявления в БД
     * @param createAds объект, содержащий необходимую информацию для обновления объявления
     * @return объект {@link Ads}, содержащий необходимую для пользователя информацию об обновленном объявлении, если пользователь авторизован на редактирование объявления и объявление обновлено.<br>
     * В противном случае <B>null</B>
     */
    @Override
    @Transactional
    public Ads updateAdvertising(int id, CreateAds createAds) {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        if (userCanChangeAdvertising(userEntity, adEntity)) {
            return adMapper.toAds(
                    adRepository.save(
                            adMapper.toAdEntity(createAds, adEntity)
                    )
            );
        }
        return null;
    }

    /**
     * Обновление изображения объявления.<br>
     * - Поиск объявления в БД по идентификатору объявления {@link AdRepository#findById(Object)}.<br>
     * - Удаление текущего изображения объявления {@link Files#deleteIfExists(Path)}, если в объявлении сохранен путь к изображению в файловой системе.<br>
     * - Создание пути для загрузки обновленного изображения объявления {@link #createPath(MultipartFile, AdEntity)}.<br>
     * - Загрузка с сайта и сохранение в файловой системе обновленного изображения объявления {@link AccountServiceImpl#uploadImage(MultipartFile, Path)}.<br>
     * - Задание необходимых параметров найденному объявлению {@link AdEntity#setUserEntity(UserEntity)}, {@link AdEntity#setImagePath(String)}, {@link AdEntity#setImageMediaType(String)}, {@link AdEntity#setImageFileSize(long)}.<br>
     * - Сохранение в БД объявления, у которого было обновлено изображение {@link AdRepository#save(Object)}.
     * @param id идентификатор объявления в БД
     * @param image загружаемое изображение
     * @return <B>true</B>
     * @throws IOException выбрасывается при ошибках, возникающих во время загрузки изображения
     */
    @Override
    @Transactional
    public boolean updateAdvertisingImage(int id, MultipartFile image) throws IOException {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        if (adEntity.getImagePath() != null) {
            Files.deleteIfExists(Path.of(adEntity.getImagePath()));
        }
        Path filePath = createPath(image, adEntity);
        adEntity.setImagePath(filePath.toAbsolutePath().toString());
        adEntity.setImageMediaType(image.getContentType());
        adEntity.setImageFileSize(image.getSize());
        adRepository.save(adEntity);
        return true;
    }

    /**
     * Удаление объявления.<br>
     * - Поиск пользователя в БД по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Поиск объявления в БД по идентификатору объявления {@link AdRepository#findById(Object)}.<br>
     * - Удаление из БД всех комментариев найденного объявления {@link CommentRepository#deleteAllByAdEntity_Id(int)}.<br>
     * - Удаление из БД объявления по id {@link AdRepository#deleteById(Object)}.<br>
     * - Удаление из файловой системы изображения объявления {@link Files#deleteIfExists(Path)}.
     * @param id идентификатор объявления в БД
     * @return <B>true</B>, если пользователь авторизован на удаление объявления и объявление удалено.<br>
     * В противном случае <B>false</B>
     * @throws IOException выбрасывается при ошибках, возникающих во время удаления изображения
     */
    @Override
    @Transactional
    public boolean deleteAdvertising(int id) throws IOException {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        if (userCanChangeAdvertising(userEntity, adEntity)) {
            commentRepository.deleteAllByAdEntity_Id(id);
            adRepository.deleteById(id);
            Files.deleteIfExists(Path.of(adEntity.getImagePath()));
            return true;
        }
        return false;
    }

    /**
     * Получение объявления по заголовку.<br>
     * - Поиск объявления в БД по заголовку объявления {@link AdRepository#findAllByTitleLike(String)}.<br>
     * - Преобразование (маппинг) списка найденных объявлений в объект возвращаемого класса {@link AdMapper#toResponseWrapperAds(List)}.
     * @param title заголовок объявления
     * @return объект {@link ResponseWrapperAds}, содержащий количество объявлений и список объявлений
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseWrapperAds findByTitle(String title) {
        return adMapper.toResponseWrapperAds(
                adRepository.findAllByTitleLike(title));
    }

    /**
     * Выгрузка изображения объявления из файловой системы.<br>
     * - Поиск объявления в БД по идентификатору объявления {@link AdRepository#findById(Object)}.<br>
     * - Копирование данных из файла изображения в ответе сервера. Входной поток получаем из метода {@link Files#newInputStream(Path, OpenOption...)}. Выходной поток получаем из метода {@link HttpServletResponse#getOutputStream()}
     * @param adId идентификатор объявления в БД
     * @param response ответ сервера
     * @throws IOException выбрасывается при ошибках, возникающих во время выгрузки изображения
     */
    @Override
    public void downloadAdImageFromFS(int adId, HttpServletResponse response) throws IOException {
        AdEntity adEntity = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));

        AccountServiceImpl.findAndDownloadImage(response,
                adEntity.getImagePath(),
                adEntity.getImageMediaType(),
                adEntity.getImageFileSize());
        log.info("The method was called to download ads image with title " + adEntity.getTitle());
    }

    /**
     * Вспомогательный метод. Создание пути для загрузки изображения объявления.<br>
     * - Создание пути из директории хранения изображений объявлений, идентификатора объявления и расширения изображения.
     * - Копирование данных из файла изображения в ответе сервера. Входной поток получаем из метода {@link Files#newInputStream(Path, OpenOption...)}. Выходной поток получаем из метода {@link HttpServletResponse#getOutputStream()}
     * @param image загружаемое изображение
     * @param adEntity объявление, для которого загружается изображение
     * @return объект класса {@link Path}
     * @throws IOException выбрасывается при ошибках, возникающих во время выгрузки изображения
     */
    private Path createPath(MultipartFile image, AdEntity adEntity) throws IOException {
        Path filePath = Path.of(adsImageDir, "Объявление_" + adEntity.getId() + "."
                + StringUtils.getFilenameExtension(image.getOriginalFilename()));
        AccountServiceImpl.uploadImage(image, filePath);
        return filePath;
    }

    /**
     * Вспомогательный метод. Проверка пользователя при редактировании или удаления объявления - данный пользователь должен быть администратором или пользователем, создавшем объявления.
     * @param user текущий пользователь
     * @param ad редактируемое/удаляемое объявление
     * @return <B>true</B>, если пользователь соответствует одному из условий.<br>
     * <B>false</B>, если пользователь не соответствует ни одному из условий.
     */
    private boolean userCanChangeAdvertising(UserEntity user, AdEntity ad) {
        return user.getRole() == Role.ADMIN || ad.getUserEntity().equals(user);
    }

    /**
     * Вспомогательный метод. Проверка пользователя при редактировании или удаления комментария - данный пользователь должен быть администратором или пользователем, создавшем комментарий.
     * @param user текущий пользователь
     * @param comment редактируемый/удаляемый комментарий
     * @return <B>true</B>, если пользователь соответствует одному из условий.<br>
     * <B>false</B>, если пользователь не соответствует ни одному из условий.
     */
    private boolean userCanChangeComment(UserEntity user, CommentEntity comment) {
        return user.getRole() == Role.ADMIN || comment.getUserEntity().equals(user);
    }
}
