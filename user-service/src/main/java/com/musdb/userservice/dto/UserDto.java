package com.musdb.userservice.dto;

import com.musdb.userservice.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String name;
    private String surname;
    private String email;
    private LocalDateTime dob;
    private String description;
    private Long profilePhotoId;
    private UserType userType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long followerCount;
    private Long followingCount;
}
