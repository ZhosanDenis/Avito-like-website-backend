package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateAds;
import ru.skypro.homework.dto.ads.FullAds;
import ru.skypro.homework.dto.ads.ResponseWrapperAds;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.service.AdMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdMapperImpl implements AdMapper {

    @Override
    public AdEntity toAdEntity(CreateAds ads, AdEntity ad) {
        ad.setDescription(ad.getDescription());
        ad.setPrice(ad.getPrice());
        ad.setTitle(ad.getTitle());
        return ad;
    }

    @Override
    public Ads toAds(AdEntity adEntity) {
        Ads ads = new Ads();
        ads.setAuthor(adEntity.getUserEntity().getId());
        ads.setPk(adEntity.getId());
        ads.setPrice(adEntity.getPrice());
        ads.setTitle(adEntity.getTitle());
        ads.setImage("ads/image" + adEntity.getId() + "/download");
        return ads;
    }

    @Override
    public FullAds toFullAds(AdEntity adEntity) {
        FullAds fullAds = new FullAds();
        fullAds.setPk(adEntity.getId());
        fullAds.setAuthorFirstName(adEntity.getUserEntity().getFirstName());
        fullAds.setAuthorLastName(adEntity.getUserEntity().getLastName());
        fullAds.setDescription(adEntity.getDescription());
        fullAds.setEmail(adEntity.getUserEntity().getEmail());
        fullAds.setImage("ads/image" + adEntity.getId() + "/download");
        fullAds.setPhone(adEntity.getUserEntity().getPhone());
        fullAds.setPrice(adEntity.getPrice());
        fullAds.setTitle(adEntity.getTitle());
        return fullAds;
    }

    @Override
    public ResponseWrapperAds toResponseWrapperAds(List<AdEntity> adEntityList) {
        ResponseWrapperAds wrapperAds = new ResponseWrapperAds();
        List<Ads> listAds = new ArrayList<>();
        wrapperAds.setCount(adEntityList.size());
        for (AdEntity adEntity : adEntityList) {
            listAds.add(toAds(adEntity));
        }
        wrapperAds.setResults(listAds);
        return wrapperAds;
    }

}
