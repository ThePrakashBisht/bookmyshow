package com.bookmyshow.userservice.controller;

import com.bookmyshow.userservice.dto.request.UpdateProfileRequest;
import com.bookmyshow.userservice.dto.response.ApiResponse;
import com.bookmyshow.userservice.dto.response.UserResponse;
import com.bookmyshow.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {
        log.info("Get current user profile request");

        UserResponse response = userService.getCurrentUser();

        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", response)
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("Update profile request");

        UserResponse response = userService.updateProfile(request);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", response)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // Only admins can access
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("Get user by ID request: {}", id);

        UserResponse response = userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", response)
        );
    }
}