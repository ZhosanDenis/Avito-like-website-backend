package ru.skypro.homework.service;

import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateAds;
import ru.skypro.homework.dto.ads.FullAds;
import ru.skypro.homework.dto.ads.ResponseWrapperAds;
import ru.skypro.homework.model.AdEntity;

import java.util.List;

public interface AdMapper {
    AdEntity toAdEntity(CreateAds ads, AdEntity ad);

    Ads toAds(AdEntity adEntity);

    FullAds toFullAds(AdEntity adEntity);

    ResponseWrapperAds toResponseWrapperAds(List<AdEntity> adEntityList);
}
