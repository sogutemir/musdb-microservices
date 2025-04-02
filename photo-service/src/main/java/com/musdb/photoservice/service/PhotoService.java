package com.musdb.photoservice.service;

import com.musdb.photoservice.dto.PhotoDto;
import com.musdb.photoservice.dto.PhotoUploadDto;

import java.util.List;

public interface PhotoService {
    PhotoDto uploadPhoto(PhotoUploadDto photoUploadDto);

    PhotoDto getPhotoById(Long photoId);

    byte[] getPhotoData(Long photoId);

    List<PhotoDto> getAllPhotos();

    List<PhotoDto> getPhotosByUserId(Long userId);

    List<PhotoDto> searchPhotosByName(String name);

    PhotoDto updatePhoto(Long photoId, PhotoDto photoDto);

    void deletePhoto(Long photoId);
}
