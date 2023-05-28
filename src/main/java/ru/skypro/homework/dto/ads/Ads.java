package ru.skypro.homework.dto.ads;

import lombok.Data;

@Data
public class Ads {
    private long author;
    private String image;
    private long pk;
    private double price;
    private String title;
}
