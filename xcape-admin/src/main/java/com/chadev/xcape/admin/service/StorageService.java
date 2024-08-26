package com.chadev.xcape.admin.service;

import com.chadev.xcape.core.domain.converter.DtoConverter;
import com.chadev.xcape.core.domain.dto.FileUploadDto;
import com.chadev.xcape.core.domain.dto.StorageDto;
import com.chadev.xcape.core.domain.entity.Storage;
import com.chadev.xcape.core.domain.type.FileType;
import com.chadev.xcape.core.exception.XcapeException;
import com.chadev.xcape.core.repository.StorageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${firebase.storage.prefix}")
    private String storagePrefix;

    private final StorageRepository storageRepository;
    private final FirebaseService firebaseService;

    private final DtoConverter dtoConverter;

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }

    private FileType convertFileType(String contentType) {
        if (contentType.startsWith("image/")) {
            return FileType.IMAGE;
        } else if (contentType.startsWith("audio/")) {
            return FileType.AUDIO;
        } else if (contentType.startsWith("video/")) {
            return FileType.VIDEO;
        } else {
            throw XcapeException.FILE_FORMAT_EXCEPTION();
        }
    }

    private String makeFileUrl(String filename) {
        return storagePrefix + URLEncoder.encode("files/" + filename, StandardCharsets.UTF_8) + "?alt=media";
    }

    @Transactional
    public void createFile(FileUploadDto fileUploadDto) {
        MultipartFile file = fileUploadDto.getFile();

        if (file == null || file.isEmpty()) {
            throw XcapeException.FILE_NOT_FOUND();
        }

        if (file.getContentType() == null) {
            throw XcapeException.FILE_FORMAT_EXCEPTION();
        }

        if (file.getOriginalFilename() == null) {
            throw XcapeException.UNNAMED_FILE();
        }

        String storedFilename = UUID.randomUUID().toString().concat(getExtension(file.getOriginalFilename()));
        firebaseService.upload(file, storedFilename);

        FileType fileType = convertFileType(file.getContentType());
        Storage storage = Storage.builder()
                .fileType(fileType)
                .filename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .url(makeFileUrl(storedFilename))
                .merchantId(fileUploadDto.getMerchantId())
                .themeId(fileUploadDto.getThemeId())
                .isUsed(true)
                .build();

        storageRepository.save(storage);
    }

    public List<StorageDto> getStorageList() {
        return storageRepository.findAll().stream().map(dtoConverter::toStorageDto).toList();
    }

    @Transactional
    public void deleteStorage(Long storageId) {
        Storage storage = storageRepository.findById(storageId).orElseThrow(XcapeException::NOT_EXISTENT_STORAGE);
        storage.setIsUsed(false);
    }
}
