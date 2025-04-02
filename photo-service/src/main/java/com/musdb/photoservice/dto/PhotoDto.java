package com.musdb.photoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {
    private Long photoId;
    private String photoName;
    private String photoExtension;
    private String photoDescription;
    private String description;
    private LocalDateTime createdAt;
    private Long userId;

    // There is gonna another endpoint for photo value.
}
