package com.musdb.userservice.repository;

import com.musdb.userservice.model.User;
import com.musdb.userservice.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUserType(UserType userType);

    Optional<User> findByUserIdAndIsDeleteFalse(Long userId);

    List<User> findByNameContainingOrSurnameContaining(String name, String surname);

    List<User> findAllByIsDeleteFalse();
}
