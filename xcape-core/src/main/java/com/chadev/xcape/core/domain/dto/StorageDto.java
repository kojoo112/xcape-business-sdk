package com.chadev.xcape.core.domain.dto;

import com.chadev.xcape.core.domain.entity.Storage;
import com.chadev.xcape.core.domain.type.FileType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageDto {

    private Long id;

    private Long merchantId;

    private Long themeId;

    private FileType fileType;

    private String url;

    private String filename;

    private String storedFilename;

    private Boolean isUsed;


    public StorageDto(Storage entity) {
        this.id = entity.getId();
        this.merchantId = entity.getMerchantId();
        this.themeId = entity.getThemeId();
        this.fileType = entity.getFileType();
        this.url = entity.getUrl();
        this.filename = entity.getFilename();
        this.storedFilename = entity.getStoredFilename();
        this.isUsed = entity.getIsUsed();
    }
}
