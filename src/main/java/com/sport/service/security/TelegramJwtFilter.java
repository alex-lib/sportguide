package com.sport.service.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sport.service.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramJwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            DecodedJWT jwt = jwtService.parseAndValidate(token);

            Long id = jwt.getClaim("id").asLong(); // ты это кладёшь
            String role = jwt.getClaim("role").asString(); // "SUBSCRIBER"/"ADMIN"
            if (id == null || role == null || role.isBlank()) {
                throw new IllegalArgumentException("JWT claims missing");
            }

            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // principal можешь сделать хоть id, хоть кастомный объект
            var auth = new UsernamePasswordAuthenticationToken(id, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            // можно не ронять запрос, просто оставляем анонимным → попадёт на 401
        }

        filterChain.doFilter(request, response);
    }
}


