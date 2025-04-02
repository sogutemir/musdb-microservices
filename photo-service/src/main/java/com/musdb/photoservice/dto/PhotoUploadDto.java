package com.musdb.photoservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PhotoUploadDto {
    @NotNull(message = "Photo file is required")
    private MultipartFile file;

    @NotBlank(message = "Photo name is required")
    private String photoName;

    private String description;

    @NotNull(message = "User ID is required")
    private Long userId;
}
