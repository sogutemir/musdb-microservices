package com.musdb.photoservice.repository;

import com.musdb.photoservice.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByUserIdAndIsDeleteFalse(Long userId);

    Optional<Photo> findByPhotoIdAndIsDeleteFalse(Long photoId);

    List<Photo> findByPhotoNameContainingAndIsDeleteFalse(String photoName);

    List<Photo> findAllByIsDeleteFalse();
}
