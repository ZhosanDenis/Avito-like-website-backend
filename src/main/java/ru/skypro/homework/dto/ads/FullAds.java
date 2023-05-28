package ru.skypro.homework.dto.ads;

import lombok.Data;

@Data
public class FullAds {
    private long pk;
    private String authorFirstName;
    private String authorLastName;
    private String description;
    private String email;
    private String image;
    private String phone;
    private double price;
    private String title;
}
