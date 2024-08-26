package com.chadev.xcape.core.domain.entity;

import com.chadev.xcape.core.domain.dto.StorageDto;
import com.chadev.xcape.core.domain.type.FileType;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Builder
@Table(name = "storage")
@AllArgsConstructor
public class Storage {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storage_id")
    private Long id;

    @Column
    private Long merchantId;

    @Column
    private Long themeId;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Column(length = 600)
    private String url;

    @Column(length = 100)
    private String filename;

    @Column(length = 50)
    private String storedFilename;

    @Column
    private Boolean isUsed;

    public Storage (StorageDto storageDto) {
        this.merchantId = storageDto.getMerchantId();
        this.themeId = storageDto.getThemeId();
        this.fileType = storageDto.getFileType();
        this.url = storageDto.getUrl();
        this.filename = storageDto.getFilename();
        this.storedFilename = storageDto.getStoredFilename();
        this.isUsed = storageDto.getIsUsed();
    }
}
