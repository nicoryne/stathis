package edu.cit.stathis.auth.service;

import edu.cit.stathis.auth.entity.User;
import edu.cit.stathis.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PhysicalIdService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Gets the physical ID of the currently authenticated user
     * @return the physical ID of the current user
     * @throws IllegalStateException if no user is authenticated or user not found
     */
    public String getCurrentUserPhysicalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getPhysicalId();
    }

    /**
     * Gets the UUID of the currently authenticated user
     * @return the UUID ID of the current user
     * @throws IllegalStateException if no user is authenticated or user not found
     */
    public UUID getCurrentUserUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getUserId();
    }

    /**
     * Gets the physical ID of a user by their email
     * @param email the email of the user
     * @return the physical ID of the user
     * @throws IllegalStateException if user not found
     */
    public String getPhysicalIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getPhysicalId();
    }

    /**
     * Gets the physical ID of a user by their UUID
     * @param userId the UUID of the user
     * @return the physical ID of the user
     * @throws IllegalStateException if user not found
     */
    public String getPhysicalIdByUserId(UUID userId) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getPhysicalId();
    }
} 