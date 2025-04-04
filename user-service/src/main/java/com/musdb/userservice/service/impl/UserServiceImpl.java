package com.musdb.userservice.service.impl;

import com.musdb.userservice.dto.LoginRequestDto;
import com.musdb.userservice.dto.LoginResponseDto;
import com.musdb.userservice.dto.UserDto;
import com.musdb.userservice.dto.UserRegistrationDto;
import com.musdb.userservice.exception.ResourceNotFoundException;
import com.musdb.userservice.exception.UserAlreadyExistsException;
import com.musdb.userservice.model.*;
import com.musdb.userservice.repository.UserCredentialRepository;
import com.musdb.userservice.repository.UserFollowerRepository;
import com.musdb.userservice.repository.UserRepository;
import com.musdb.userservice.security.JwtService;
import com.musdb.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


//TODO: Validasyonlar servisten ayrılıp validation klasörü oluşturulup oradan olarak çekilsin.
//TODO: @RequiredArgsConstructor kullanılabilir.
//TODO: Auth servisi buradan ayrılacak.
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserFollowerRepository userFollowerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           UserCredentialRepository userCredentialRepository,
                           UserFollowerRepository userFollowerRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.userFollowerRepository = userFollowerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    //TODO: Entity DTO set işlemleri MApper ile yapılacka.

    @Override
    @Transactional
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        if (userCredentialRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        User user = new User();
        user.setName(registrationDto.getName());
        user.setSurname(registrationDto.getSurname());
        user.setDob(registrationDto.getDob());
        user.setDescription(registrationDto.getDescription());
        user.setEmail(registrationDto.getEmail());
        user.setUserType(registrationDto.getUserType());

        User savedUser = userRepository.save(user);

        UserCredential userCredential = new UserCredential();
        userCredential.setUser(savedUser);
        userCredential.setUsername(registrationDto.getUsername());
        userCredential.setPassword(passwordEncoder.encode(registrationDto.getPassword()));


        userCredentialRepository.save(userCredential);

        return mapToDto(savedUser);
    }

    @Override
    public LoginResponseDto loginUser(LoginRequestDto loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        UserCredential userCredential = userCredentialRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Username not found"));

        User user = userCredential.getUser();

        String token = jwtService.generateToken(
                loginRequest.getUsername(),
                user.getUserId(),
                user.getUserType().name()
        );

        UserDto userDto = mapToDto(user);

        return LoginResponseDto.builder()
                .token(token)
                .user(userDto)
                .build();
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findByUserIdAndIsDeleteFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserDto userDto = mapToDto(user);
        userDto.setFollowerCount(getFollowerCount(userId));
        userDto.setFollowingCount(getFollowingCount(userId));

        return userDto;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAllByIsDeleteFalse().stream()
                .map(this::mapToDto)
                .toList();

        /*return userRepository.findAll().stream()
                .filter(user -> !user.getIsDelete())
                .map(this::mapToDto)
                .toList();*/
    }

    @Override
    public List<UserDto> getUsersByType(UserType userType) {
        return userRepository.findByUserType(userType).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<UserDto> searchUsers(String query) {
        return userRepository.findByNameContainingOrSurnameContaining(query, query).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findByUserIdAndIsDeleteFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setDescription(userDto.getDescription());

        if (userDto.getDob() != null) {
            user.setDob(userDto.getDob());
        }

        user.setEmail(userDto.getEmail());

        if (userDto.getProfilePhotoId() != null) {
            user.setProfilePhotoId(userDto.getProfilePhotoId());
        }

        User updatedUser = userRepository.save(user);

        return mapToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findByUserIdAndIsDeleteFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsDelete(true);
        userRepository.save(user);
    }


    //TODO: Dönüş tipi değiştirilebilir.
    @Override
    @Transactional
    public boolean followUser(Long userId, Long followingUserId) {
        if (userId.equals(followingUserId)) {
            return false;
        }

        User user = userRepository.findByUserIdAndIsDeleteFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User followingUser = userRepository.findByUserIdAndIsDeleteFalse(followingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Following user not found"));

        UserFollowerKey key = new UserFollowerKey(userId, followingUserId);

        UserFollower userFollower = userFollowerRepository.findById(key)
                .orElse(new UserFollower());

        if (userFollower.getId() == null) {
            userFollower.setId(key);
            userFollower.setUser(user);
            userFollower.setFollowingUser(followingUser);
        }

        userFollower.setIsStillFollowing(true);

        userFollowerRepository.save(userFollower);
        return true;
    }

    @Override
    @Transactional
    public boolean unfollowUser(Long userId, Long followingUserId) {
        UserFollowerKey key = new UserFollowerKey(userId, followingUserId);

        UserFollower userFollower = userFollowerRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("User not following"));

        userFollower.setIsStillFollowing(false);

        userFollowerRepository.save(userFollower);
        return true;
    }

    @Override
    public List<UserDto> getFollowers(Long userId) {
        User user = userRepository.findByUserIdAndIsDeleteFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userFollowerRepository.findByFollowingUserAndIsStillFollowingTrue(user).stream()
                .map(following -> mapToDto(following.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getFollowings(Long userId) {
        User user = userRepository.findByUserIdAndIsDeleteFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userFollowerRepository.findByUserAndIsStillFollowingTrue(user).stream()
                .map(following -> mapToDto(following.getFollowingUser()))
                .collect(Collectors.toList());
    }

    @Override
    public Long getFollowerCount(Long userId) {
        User user = userRepository.findByUserIdAndIsDeleteFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userFollowerRepository.countByFollowingUserAndIsStillFollowingTrue(user);
    }

    @Override
    public Long getFollowingCount(Long userId) {
        User user = userRepository.findByUserIdAndIsDeleteFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userFollowerRepository.countByUserAndIsStillFollowingTrue(user);
    }

    //TODO: kaldırılıp mappera taşınacak.

    // Helper method to map User entity to UserDto
    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .surname(user.getSurname())
                .dob(user.getDob())
                .description(user.getDescription())
                .email(user.getEmail())
                .profilePhotoId(user.getProfilePhotoId())
                .userType(user.getUserType())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
