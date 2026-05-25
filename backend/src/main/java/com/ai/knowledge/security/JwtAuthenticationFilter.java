package com.ai.knowledge.security;

import com.ai.knowledge.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                if (JwtUtil.validateToken(token)) {
                    Long userId = JwtUtil.getUserId(token);
                    String username = JwtUtil.getUsername(token);
                    Integer userType = JwtUtil.getUserType(token);
                    
                    UserInfo userInfo = new UserInfo(userId, username, userType);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userInfo, null, 
                            Collections.singletonList(new SimpleGrantedAuthority(
                                userType == 1 ? "ROLE_ADMIN" : "ROLE_USER"
                            )));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token invalid, continue without authentication
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    public static class UserInfo {
        private final Long userId;
        private final String username;
        private final Integer userType;
        
        public UserInfo(Long userId, String username, Integer userType) {
            this.userId = userId;
            this.username = username;
            this.userType = userType;
        }
        
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public Integer getUserType() { return userType; }
    }
}
