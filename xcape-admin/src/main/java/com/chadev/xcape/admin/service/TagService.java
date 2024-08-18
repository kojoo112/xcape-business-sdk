package com.chadev.xcape.admin.service;

import com.chadev.xcape.core.domain.dto.TagDto;
import com.chadev.xcape.core.domain.entity.Tag;
import com.chadev.xcape.core.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagDto> getTagList() {
        return tagRepository.findAll().stream().map(TagDto::new).toList();
    }

    public List<TagDto> getTagListByThemeId(Long themeId) {
        return tagRepository.findByThemeId(themeId).stream().map(TagDto::new).toList();
    }

    @Transactional
    public void createTag(TagDto tagDto) {
        Tag newTag = Tag.builder()
                .name(tagDto.getName())
                .merchantId(tagDto.getMerchantId())
                .themeId(tagDto.getThemeId())
                .build();

        tagRepository.save(newTag);
    }
}
