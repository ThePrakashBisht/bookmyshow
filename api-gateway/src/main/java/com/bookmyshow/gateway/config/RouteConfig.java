package com.bookmyshow.gateway.config;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteConfig {

    // Public endpoints - no authentication required
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            // Auth endpoints
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/forgot-password",

            // Public event browsing
            "/api/cities",
            "/api/events",
            "/api/events/type",
            "/api/events/search",
            "/api/events/now-showing",
            "/api/events/coming-soon",
            "/api/events/city",
            "/api/events/filter",

            // Public show browsing
            "/api/shows",
            "/api/shows/upcoming",
            "/api/shows/event",
            "/api/shows/venue",

            // Public venue browsing
            "/api/venues",
            "/api/venues/city",

            // Health & fallback
            "/fallback",
            "/actuator",
            "/health"
    );

    // Admin only endpoints
    private static final List<String> ADMIN_ENDPOINTS = List.of(
            "/api/admin",
            "/api/users/all"  // List all users - admin only
    );

    // Endpoints that require ownership check (user can only access their own)
    private static final List<String> OWNER_ENDPOINTS = List.of(
            "/api/users/profile",
            "/api/bookings/user"
    );

    public boolean isPublicPath(String path) {
        if (path == null) return false;

        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> pathMatchesPattern(path, endpoint));
    }

    public boolean isAdminPath(String path) {
        if (path == null) return false;

        return ADMIN_ENDPOINTS.stream()
                .anyMatch(endpoint -> pathMatchesPattern(path, endpoint));
    }

    public boolean isOwnerPath(String path) {
        if (path == null) return false;

        return OWNER_ENDPOINTS.stream()
                .anyMatch(endpoint -> pathMatchesPattern(path, endpoint));
    }

    private boolean pathMatchesPattern(String path, String pattern) {
        // Handle exact match
        if (path.equals(pattern)) {
            return true;
        }

        // Handle prefix match (pattern should match start of path)
        if (path.startsWith(pattern + "/") || path.startsWith(pattern + "?")) {
            return true;
        }

        // Handle GET requests for resources with IDs
        // e.g., /api/events/1 should match /api/events
        String[] pathParts = path.split("/");
        String[] patternParts = pattern.split("/");

        if (pathParts.length > patternParts.length) {
            StringBuilder matchPath = new StringBuilder();
            for (int i = 0; i < patternParts.length; i++) {
                matchPath.append("/").append(pathParts[i + 1]);
            }
            // Remove leading slash if pattern doesn't have it
            String constructedPath = matchPath.toString();
            if (!pattern.startsWith("/")) {
                constructedPath = constructedPath.substring(1);
            }
            return constructedPath.equals(pattern);
        }

        return false;
    }
}