package com.musdb.userservice.controller;

import com.musdb.userservice.dto.UserDto;
import com.musdb.userservice.dto.UserRegistrationDto;
import com.musdb.userservice.model.UserType;
import com.musdb.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        return new ResponseEntity<>(userService.registerUser(registrationDto), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.isUserSelf(#userId)")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/type/{userType}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersByType(@PathVariable UserType userType) {
        return ResponseEntity.ok(userService.getUsersByType(userType));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDto>> searchUser(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.isUserSelf(#userId)")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(userId, userDto));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/follow/{followingUserId}")
    @PreAuthorize("@userSecurity.isUserSelf(#userId)")
    public ResponseEntity<Boolean> followUser(@PathVariable Long userId, @PathVariable Long followingUserId) {
        return ResponseEntity.ok(userService.followUser(userId, followingUserId));
    }

    @PostMapping("/{userId}/unfollow/{followingUserId}")
    @PreAuthorize("@userSecurity.isUserSelf(#userId)")
    public ResponseEntity<Boolean> unfollowUser(@PathVariable Long userId, @PathVariable Long followingUserId) {
        return ResponseEntity.ok(userService.unfollowUser(userId, followingUserId));
    }

    @PostMapping("/{userId}/followers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDto>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFollowers(userId));
    }

    @PostMapping("/{userId}/followings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDto>> getFollowings(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFollowings(userId));
    }

    @GetMapping("/{userId}/followers-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getFollowersCount(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFollowerCount(userId));
    }

    @GetMapping("/{userId}/followings-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFollowingCount(userId));
    }
}
