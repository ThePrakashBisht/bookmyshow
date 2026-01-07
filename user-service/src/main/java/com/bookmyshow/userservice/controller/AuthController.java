package com.bookmyshow.userservice.controller;

import com.bookmyshow.userservice.dto.request.LoginRequest;
import com.bookmyshow.userservice.dto.request.RegisterRequest;
import com.bookmyshow.userservice.dto.response.ApiResponse;
import com.bookmyshow.userservice.dto.response.AuthResponse;
import com.bookmyshow.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request received for email: {}", request.getEmail());

        AuthResponse response = authService.register(request);

        return new ResponseEntity<>(
                ApiResponse.success("User registered successfully", response),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request received for email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }
}