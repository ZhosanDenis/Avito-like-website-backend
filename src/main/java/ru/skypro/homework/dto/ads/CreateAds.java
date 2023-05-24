package ru.skypro.homework.dto.ads;

import lombok.Data;

@Data
public class CreateAds {
    private String description;
    private double price;
    private String title;
}
