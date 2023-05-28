package ru.skypro.homework.dto.comment;

import lombok.Data;

@Data
public class Comment {
    private long author;
    private String authorImage;
    private String authorFirstName;
    private long createdAt;
    private long pk;
    private String text;
}
