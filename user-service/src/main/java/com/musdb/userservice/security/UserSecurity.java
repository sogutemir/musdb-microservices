package com.musdb.userservice.security;

import com.musdb.userservice.model.User;
import com.musdb.userservice.model.UserCredential;
import com.musdb.userservice.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {
    private final UserCredentialRepository userCredentialRepository;

    @Autowired
    public UserSecurity(UserCredentialRepository userCredentialRepository) {
        this.userCredentialRepository = userCredentialRepository;
    }

    public Boolean isUserSelf(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String username = userDetails.getUsername();

            UserCredential userCredential = userCredentialRepository.findByUsername(username).orElseThrow(null);
            if (userCredential != null) {
                User user = userCredential.getUser();
                return user.getUserId().equals(userId);
            }
        }

        return false;
    }
}
