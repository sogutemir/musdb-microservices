package com.musdb.userservice.repository;

import com.musdb.userservice.model.User;
import com.musdb.userservice.model.UserFollower;
import com.musdb.userservice.model.UserFollowerKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFollowerRepository extends JpaRepository<UserFollower, UserFollowerKey> {
    List<UserFollower> findByFollowingUserAndIsStillFollowingTrue(User followingUser);

    List<UserFollower> findByUserAndIsStillFollowingTrue(User user);

    long countByFollowingUserAndIsStillFollowingTrue(User user);

    long countByUserAndIsStillFollowingTrue(User user);
}
