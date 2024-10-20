package com.chadev.xcape.admin.service;

import com.chadev.xcape.admin.util.S3Uploader;
import com.chadev.xcape.core.domain.converter.DtoConverter;
import com.chadev.xcape.core.domain.dto.ThemeDto;
import com.chadev.xcape.core.domain.entity.Ability;
import com.chadev.xcape.core.domain.entity.Merchant;
import com.chadev.xcape.core.domain.entity.Theme;
import com.chadev.xcape.core.domain.request.ThemeModifyRequestDto;
import com.chadev.xcape.core.repository.AbilityRepository;
import com.chadev.xcape.core.repository.MerchantRepository;
import com.chadev.xcape.core.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@EnableCaching
@RequiredArgsConstructor
public class ThemeService {

	private final MerchantRepository merchantRepository;
	private final ThemeRepository themeRepository;
	private final AbilityRepository abilityRepository;
	private final AbilityService abilityService;
	private final S3Uploader s3Uploader;
	private final DtoConverter dtoConverter;

	@Transactional
	public ThemeDto createThemeByMerchantId(Long merchantId, ThemeModifyRequestDto requestDto,
		MultipartHttpServletRequest request) throws IOException {
		Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(IllegalArgumentException::new);

		MultipartFile mainImage = request.getFile("mainImage");
		MultipartFile bgImage = request.getFile("bgImage");
		Theme newTheme = Theme.builder()
			.merchant(merchant)
			.bgImagePath(requestDto.getBgImagePath())
			.colorCode(requestDto.getColorCode())
			.description(requestDto.getDescription())
			.difficulty(requestDto.getDifficulty())
			.genre(requestDto.getGenre())
			.hasXKit(requestDto.getHasXKit())
			.isCrimeScene(requestDto.getIsCrimeScene())
			.useYn(requestDto.getUseYn())
			.mainImagePath(requestDto.getMainImagePath())
			.minParticipantCount(requestDto.getMinParticipantCount())
			.maxParticipantCount(requestDto.getMaxParticipantCount())
			.nameKo(requestDto.getNameKo())
			.nameEn(requestDto.getNameEn())
			.point(requestDto.getPoint())
			.youtubeLink(requestDto.getYoutubeLink())
			.runningTime(requestDto.getRunningTime())
			.build();

		requestDto.getAbilityList().stream().map(abilityDto -> Ability.builder()
				.code(abilityDto.getCode())
				.name(abilityDto.getName())
				.value(abilityDto.getValue())
				.merchant(merchant)
				.theme(newTheme)
				.build())
			.forEach(abilityRepository::save);

		Theme savedTheme = themeRepository.save(newTheme);

		String mainImageURL = s3Uploader.upload(mainImage, Long.toString(savedTheme.getId()));
		String bgImageURL = s3Uploader.upload(bgImage, Long.toString(savedTheme.getId()));

		savedTheme.setMainImagePath(mainImageURL);
		savedTheme.setBgImagePath(bgImageURL);
		return dtoConverter.toThemeDto(themeRepository.save(savedTheme));
	}

	@Transactional
	public void modifyThemeDetail(Long themeId, ThemeModifyRequestDto requestDto,
		MultipartHttpServletRequest request) throws IOException {
		Theme updatedTheme = themeRepository.findById(themeId).orElseThrow(IllegalArgumentException::new);
		themeImageUpload(requestDto, request);
		if (requestDto.getMainImagePath() != null) {
			updatedTheme.setMainImagePath(requestDto.getMainImagePath());
		}
		if (requestDto.getBgImagePath() != null) {
			updatedTheme.setBgImagePath(requestDto.getBgImagePath());
		}
		updatedTheme.update(requestDto);
		abilityService.saveAbilityList(requestDto.getAbilityList(), updatedTheme);
	}

	public void themeImageUpload(ThemeModifyRequestDto requestDto, MultipartHttpServletRequest request) throws
		IOException {
		MultipartFile mainImage = request.getFile("mainImage");
		MultipartFile bgImage = request.getFile("bgImage");
		if (mainImage != null && !mainImage.isEmpty()) {
			requestDto.setMainImagePath(s3Uploader.upload(mainImage, Long.toString(requestDto.getThemeId())));
		}
		if (bgImage != null && !bgImage.isEmpty()) {
			requestDto.setBgImagePath(s3Uploader.upload(bgImage, Long.toString(requestDto.getThemeId())));
		}
	}

	public ThemeDto getThemeById(Long themeId) {
		Theme theme = themeRepository.findById(themeId).orElseThrow(IllegalArgumentException::new);
		return dtoConverter.toThemeDto(theme);
	}

	public ThemeDto getThemeDetail(Long themeId) {
		ThemeDto theme = getThemeById(themeId);
		theme.setAbilityList(abilityService.getAbilityListByThemeId(themeId));
		return theme;
	}

	public List<ThemeDto> getThemeListByMerchantId(Long merchantId) {
		return themeRepository.findThemesByMerchantId(merchantId).stream().map(dtoConverter::toThemeDto).toList();
	}

	public List<ThemeDto> getThemeList() {
		return themeRepository.findAll()
			.stream()
			.filter(Theme::getUseYn)
			.map(dtoConverter::toThemeDto)
			.toList();
	}
}
