package com.banking.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // ✅ Bỏ qua các endpoint public (login, register)
        if (path.contains("/api/auth/login") || path.contains("/api/users/register") || path.contains("/api/public")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⚠️ No Authorization header found for path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        // ✅ Validate token trước khi parse
        if (!jwtUtil.validateToken(token)) {
            System.out.println(" Invalid or expired token for path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Lấy thông tin từ token
        Claims claims = jwtUtil.getClaims(token);
        String username = claims.getSubject();
        String role = (String) claims.get("role");

        System.out.println(" Token accepted. User: " + username + ", Role: " + role + ", Path: " + path);

        // ✅ Nếu chưa có Authentication trong SecurityContext thì thiết lập
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            System.out.println(" Authentication set for user: " + username);
        }
        System.out.println("🔹 Path: " + path);
        System.out.println("🔹 Authorization: " + authHeader);
        System.out.println("🔹 Token validation: " + jwtUtil.validateToken(token));
        System.out.println("🔹 Username: " + username + ", Role: " + role);
        System.out.println("🔹 Context auth before filter: " + SecurityContextHolder.getContext().getAuthentication());

        filterChain.doFilter(request, response);

    }
}
