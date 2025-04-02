package com.musdb.photoservice.controller;

import com.musdb.photoservice.dto.PhotoDto;
import com.musdb.photoservice.dto.PhotoUploadDto;
import com.musdb.photoservice.service.PhotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {
    private final PhotoService photoService;

    @Autowired
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoDto> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("photoName") String photoName,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "description", required = false) String description) {
        PhotoUploadDto photoUploadDto = new PhotoUploadDto();
        photoUploadDto.setFile(file);
        photoUploadDto.setPhotoName(photoName);
        photoUploadDto.setDescription(description);
        photoUploadDto.setUserId(userId);

        return new ResponseEntity<>(photoService.uploadPhoto(photoUploadDto), HttpStatus.CREATED);
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<PhotoDto> getPhotoById(@PathVariable Long photoId) {
        return ResponseEntity.ok(photoService.getPhotoById(photoId));
    }

    @GetMapping("/{photoId}/view")
    public ResponseEntity<byte[]>  viewPhoto(@PathVariable Long photoId) {
        byte[] photoData = photoService.getPhotoData(photoId);
        PhotoDto photoDto = new PhotoDto();

        HttpHeaders headers = new HttpHeaders();
        String contentType = "image/jpeg";

        if (photoDto.getPhotoDescription() != null) {
            contentType = switch (photoDto.getPhotoDescription().toLowerCase()) {
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "jpg", "jpeg" -> "image/jpeg";
                default -> contentType;
            };
        }

        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", photoDto.getPhotoName());

        return new ResponseEntity<>(photoData, headers, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<PhotoDto>> getAllPhotos() {
        return ResponseEntity.ok(photoService.getAllPhotos());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PhotoDto>> getPhotosByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(photoService.getPhotosByUserId(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PhotoDto>> searchPhotosByName(@RequestParam String name) {
        return ResponseEntity.ok(photoService.searchPhotosByName(name));
    }

    @PutMapping("/{photoId}")
    public ResponseEntity<PhotoDto> updatePhoto(@PathVariable Long photoId, @Valid @RequestBody PhotoDto photoDto) {
        return ResponseEntity.ok(photoService.updatePhoto(photoId, photoDto));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) {
        photoService.deletePhoto(photoId);
        return ResponseEntity.noContent().build();
    }
}
