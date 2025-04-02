package com.musdb.photoservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId;

    @Column(name = "photo_name")
    private String photoName;

    @Column(name = "photo_extension")
    private String photoExtension;

    @Column(name = "file_path")
    private String filePath;  // Binary data yerine dosya yolu

    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_delete")
    private Boolean isDelete = false;

    @Column(name = "user_id")
    private Long userId;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}