package com.chadev.xcape.api.service;

import com.chadev.xcape.core.domain.converter.DtoConverter;
import com.chadev.xcape.core.domain.dto.HintDto;
import com.chadev.xcape.core.domain.dto.PriceDto;
import com.chadev.xcape.core.domain.dto.ThemeDto;
import com.chadev.xcape.core.domain.entity.Theme;
import com.chadev.xcape.core.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final AbilityService abilityService;
    private final DtoConverter dtoConverter;
    private final HintService hintService;

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

    public List<ThemeDto> getThemeListWithHintList() {
        List<ThemeDto> themeDtoList = getAllThemeList();
        List<HintDto> hintDtoList = hintService.getHintList();
        List<HintDto> newHintList;

        for (int i = 0; i < themeDtoList.size(); i++) {
            ThemeDto themeDto = themeDtoList.get(i);
            newHintList = new ArrayList<>();

            for (int j = 0; j < hintDtoList.size(); j++) {
                HintDto hintDto = hintDtoList.get(j);
                if (themeDto.getId().equals(hintDto.getThemeId())) {
                    newHintList.add(hintDto);
                }
                themeDtoList.get(i).setHintList(newHintList);
            }
        }
        return themeDtoList;
    }
}
