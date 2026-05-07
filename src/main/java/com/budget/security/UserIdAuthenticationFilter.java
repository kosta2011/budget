package com.budget.security;

import com.budget.entity.User;
import com.budget.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class UserIdAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final String internalToken;

    public UserIdAuthenticationFilter(UserRepository userRepository,
                                      @Value("${app.internal.token}") String internalToken) {
        this.userRepository = userRepository;
        this.internalToken = internalToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Проверяем наличие и корректность внутреннего токена
        String providedToken = request.getHeader("X-Internal-Token");
        if (providedToken != null && providedToken.equals(internalToken)) {
            String userId = request.getHeader("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
