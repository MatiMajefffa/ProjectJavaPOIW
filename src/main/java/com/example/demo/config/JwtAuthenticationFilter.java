package com.example.demo.config;

import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Bezpiecznik dla CORS Preflight – wpuszczamy zapytania OPTIONS bez tokenu
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Wywołanie metody wyciągającej e-mail z tokenu
                String email = jwtService.extractUsername(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Budujemy obiekt uwierzytelnienia użytkownika dla kontekstu Spring Security
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ Pomyślnie autoryzowano użytkownika: " + email);
                }
            } catch (Exception e) {
                // Wypisujemy błąd na konsolę serwera, żebyś widział dlaczego token został odrzucony!
                System.err.println("❌ Błąd walidacji tokenu JWT: " + e.getMessage());
                SecurityContextHolder.clearContext(); // Czyścimy kontekst przy błędnym tokenie
            }
        } else {
            System.out.println("ℹ️ Brak nagłówka Authorization lub błędny format dla ścieżki: " + request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}