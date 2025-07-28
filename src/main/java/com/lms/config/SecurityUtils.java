package com.lms.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        
        return authentication.getName();
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.getAuthorities().stream()
                   .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public static boolean isInstructor() {
        return hasRole("INSTRUCTOR") || isAdmin();
    }

    public static boolean isStudent() {
        return hasRole("STUDENT");
    }

    public static boolean canAccessResource(String username) {
        return isAdmin() || getCurrentUsername().equals(username);
    }
}
