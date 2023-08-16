package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateAds;
import ru.skypro.homework.dto.ads.FullAds;
import ru.skypro.homework.dto.ads.ResponseWrapperAds;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.CreateComment;
import ru.skypro.homework.dto.comment.ResponseWrapperComment;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AdService {

    /**
     * Получение всех комментариев по id объявления
     * @param id идентификатор объявления в БД
     */
    ResponseWrapperComment getComments(Integer id);

    /**
     * Создание комментария к выбранному объявлению
     * @param id идентификатор объявления в БД
     * @param createComment объект, содержащий текст комментария
     */
    Comment addComment(Integer id, CreateComment createComment);

    /**
     * Удаления комментария выбранного объявления
     * @param adId идентификатор объявления в БД
     * @param commentId идентификатор комментария в БД
     */
    boolean deleteComment(Integer adId, Integer commentId);

    /**
     * Обновление комментария выбранного объявления
     * @param adId идентификатор объявления в БД
     * @param commentId идентификатор комментария в БД
     * @param createComment объект, содержащий текст комментария
     */
    Comment updateComment(Integer adId, Integer commentId, CreateComment createComment);

    /**
     * Создание объявления
     * @param createAds объект, содержащий необходимую информацию для создания объявления
     * @param image загружаемое изображение
     * @throws IOException выбрасывается при ошибках, возникающих во время загрузки изображения
     */
    Ads addAdvertising(CreateAds createAds, MultipartFile image) throws IOException;

    /**
     * Получение объявления по id
     * @param id идентификатор объявления в БД
     */
    FullAds getAdvertising(int id);

    /**
     * Получение всех объявлений
     */
    ResponseWrapperAds getAllAdvertising();

    /**
     * Получение всех объявлений аутентифицированного пользователя
     */
    ResponseWrapperAds getAllMyAdvertising();

    /**
     * Обновление объявления
     * @param id идентификатор объявления в БД
     * @param createAds объект, содержащий необходимую информацию для обновления объявления
     */
    Ads updateAdvertising(int id, CreateAds createAds);

    /**
     * Обновление изображения объявления
     * @param id идентификатор объявления в БД
     * @param image загружаемое изображение
     * @throws IOException выбрасывается при ошибках, возникающих во время загрузки изображения
     */
    boolean updateAdvertisingImage(int id, MultipartFile image) throws IOException;

    /**
     * Удаление объявления
     * @param id идентификатор объявления в БД
     * @throws IOException выбрасывается при ошибках, возникающих во время удаления изображения
     */
    boolean deleteAdvertising(int id) throws IOException;

    /**
     * Получение объявления по заголовку
     * @param title заголовок объявления
     */
    ResponseWrapperAds findByTitle(String title);

    /**
     * Выгрузка изображения объявления из файловой системы
     * @param adId идентификатор объявления в БД
     * @param response ответ сервера
     * @throws IOException выбрасывается при ошибках, возникающих во время выгрузки изображения
     */
    void downloadAdImageFromFS(int adId, HttpServletResponse response) throws IOException;
}
