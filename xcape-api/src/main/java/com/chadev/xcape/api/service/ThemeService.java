package com.chadev.xcape.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chadev.xcape.core.domain.converter.DtoConverter;
import com.chadev.xcape.core.domain.dto.PriceDto;
import com.chadev.xcape.core.domain.dto.ThemeDto;
import com.chadev.xcape.core.domain.entity.Theme;
import com.chadev.xcape.core.repository.ThemeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final AbilityService abilityService;
    private final DtoConverter dtoConverter;

    public ThemeDto getThemeById(Long themeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow(IllegalArgumentException::new);
        return dtoConverter.toThemeDto(theme);
    }

    public List<ThemeDto> getThemeIdListByMerchantId(Long merchantId) {
        List<Theme> themeList = themeRepository.findThemesWithPriceListByMerchantId(merchantId);
        return themeList.stream()
                        .filter(Theme::getUseYn)
                        .map(theme -> {
                            ThemeDto themeDto = dtoConverter.toThemeDto(theme);
                            List<PriceDto> priceDtoList = theme.getPriceList()
                                                               .stream()
                                                               .map(dtoConverter::toPriceDto).toList();
                            themeDto.setPriceList(priceDtoList);
                            return themeDto;
                        }).toList();
    }

    public ThemeDto getThemeDetail(Long themeId) {
        ThemeDto theme = getThemeById(themeId);
        theme.setAbilityList(abilityService.getAbilityListByThemeId(themeId));
        return theme;
    }

    public List<ThemeDto> getAllThemeList() {
        return themeRepository.findAll()
                              .stream()
                              .filter(Theme::getUseYn)
                              .map(dtoConverter::toThemeDto).toList();
    }
}
