package com.musdb.photoservice.service.impl;

import com.musdb.photoservice.dto.PhotoDto;
import com.musdb.photoservice.dto.PhotoUploadDto;
import com.musdb.photoservice.exception.FileStorageException;
import com.musdb.photoservice.exception.ResourceNotFoundException;
import com.musdb.photoservice.model.Photo;
import com.musdb.photoservice.repository.PhotoRepository;
import com.musdb.photoservice.service.PhotoService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;

    @Value("${photo.upload.dir}")
    private String uploadDir;

    @Autowired
    public PhotoServiceImpl(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    @Override
    @Transactional
    public PhotoDto uploadPhoto(PhotoUploadDto photoUploadDto) {
        try {
            // Yükleme dizininin varlığını kontrol et, yoksa oluştur
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Gelen dosyadan bilgileri çıkar
            MultipartFile file = photoUploadDto.getFile();
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);

            // Benzersiz bir dosya adı oluştur
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
            Path targetLocation = uploadPath.resolve(uniqueFilename);

            // Dosyayı diske kopyala
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Photo nesnesini oluştur
            Photo photo = new Photo();
            photo.setPhotoName(photoUploadDto.getPhotoName());
            photo.setDescription(photoUploadDto.getDescription());
            photo.setPhotoExtension(extension);
            photo.setFilePath(uniqueFilename); // Dosya adını veritabanına kaydet
            photo.setUserId(photoUploadDto.getUserId());
            photo.setIsDelete(false);

            // Veritabanına kaydet
            Photo savedPhoto = photoRepository.save(photo);

            return mapToDto(savedPhoto);

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PhotoDto getPhotoById(Long photoId) {
        Photo photo = photoRepository.findByPhotoIdAndIsDeleteFalse(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        return mapToDto(photo);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getPhotoData(Long photoId) {
        try {
            Photo photo = photoRepository.findByPhotoIdAndIsDeleteFalse(photoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

            // Dosya yolunu veritabanından al
            String filePath = photo.getFilePath();

            // Dosya yolunu oluştur
            Path path = Paths.get(uploadDir, filePath);

            // Dosya varsa içeriğini oku ve byte[] olarak döndür
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            } else {
                throw new FileStorageException("File not found: " + filePath);
            }
        } catch (IOException e) {
            throw new FileStorageException("Could not read file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoDto> getAllPhotos() {
        return photoRepository.findAllByIsDeleteFalse().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoDto> getPhotosByUserId(Long userId) {
        return photoRepository.findByUserIdAndIsDeleteFalse(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoDto> searchPhotosByName(String name) {
        return photoRepository.findByPhotoNameContainingAndIsDeleteFalse(name).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PhotoDto updatePhoto(Long photoId, PhotoDto photoDto) {
        Photo photo = photoRepository.findByPhotoIdAndIsDeleteFalse(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        // Sadece metin alanlarını güncelle
        photo.setPhotoName(photoDto.getPhotoName());
        photo.setDescription(photoDto.getDescription());

        Photo updatedPhoto = photoRepository.save(photo);
        return mapToDto(updatedPhoto);
    }

    @Override
    @Transactional
    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findByPhotoIdAndIsDeleteFalse(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        try {
            // Dosya sisteminden fiziksel dosyayı silmeye çalış
            Path filePath = Paths.get(uploadDir, photo.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Dosya silme hatası olursa sadece log'la, işlemi durdurmuyoruz
            System.err.println("Could not delete file: " + e.getMessage());
        }

        // Mantıksal silme işlemi - veritabanında kaydı silmiyor, sadece flag'i değiştiriyor
        photo.setIsDelete(true);
        photoRepository.save(photo);
    }

    private PhotoDto mapToDto(Photo photo) {
        return PhotoDto.builder()
                .photoId(photo.getPhotoId())
                .photoName(photo.getPhotoName())
                .photoExtension(photo.getPhotoExtension())
                .description(photo.getDescription())
                .createdAt(photo.getCreatedAt())
                .userId(photo.getUserId())
                .build();
    }
}