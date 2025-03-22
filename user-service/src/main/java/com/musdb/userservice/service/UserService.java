package com.musdb.userservice.service;

import com.musdb.userservice.dto.LoginRequestDto;
import com.musdb.userservice.dto.LoginResponseDto;
import com.musdb.userservice.dto.UserDto;
import com.musdb.userservice.dto.UserRegistrationDto;
import com.musdb.userservice.model.UserType;

import java.util.List;

public interface UserService {
    UserDto registerUser(UserRegistrationDto registrationDto);

    LoginResponseDto loginUser(LoginRequestDto loginRequest);

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    List<UserDto> getUsersByType(UserType userType);

    List<UserDto> searchUsers(String query);

    UserDto updateUser(Long userId, UserDto userDto);

    void deleteUser(Long userId);

    boolean followUser(Long userId, Long followingUserId);

    boolean unfollowUser(Long followerId, Long followingUserId);

    List<UserDto> getFollowers(Long userId);

    List<UserDto> getFollowings(Long userId);

    Long getFollowerCount(Long userId);

    Long getFollowingCount(Long userId);
}
