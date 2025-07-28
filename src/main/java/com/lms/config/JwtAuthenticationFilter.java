package com.lms.config;

import com.lms.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            String jwt = extractJwtFromRequest(request);
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtService.extractUsername(jwt);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                        authenticateUser(userDetails, request);
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            logger.error("JWT token has expired", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has expired");
            return;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token signature");
            return;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token format");
            return;
        } catch (Exception e) {
            logger.error("Unable to process JWT token", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void authenticateUser(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
