package com.example.makaosafe.config;

import com.example.makaosafe.security.CustomUserDetailsService;
import com.example.makaosafe.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        /* 1. PUBLIC AUTH ENDPOINTS */
                        .requestMatchers("/api/auth/**").permitAll()

                        /* 2. PUBLIC PROPERTY ENDPOINTS (TENANT VIEW) */
                        // This allows GET /api/properties, GET /api/properties/1, GET /api/properties/search, etc.
                        .requestMatchers(HttpMethod.GET, "/api/properties/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/properties").permitAll()

                        /* 3. PUBLIC CALLBACKS */
                        .requestMatchers("/api/payment/callback").permitAll()

                        /* 4. LANDLORD PROTECTED ENDPOINTS */
                        // Explicitly check for both prefixed and non-prefixed roles
                        .requestMatchers(HttpMethod.POST, "/api/properties/**").hasAnyAuthority("ROLE_LANDLORD", "LANDLORD")
                        .requestMatchers("/api/properties/my-listings").hasAnyAuthority("ROLE_LANDLORD", "LANDLORD")

                        /* 5. AUTHENTICATED USER ENDPOINTS */
                        .requestMatchers("/api/bookings/**").authenticated()
                        .requestMatchers("/api/payment/**").authenticated()

                        /* 6. CATCH-ALL */
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // List all possible origins
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "https://makao-safe.vercel.app",
                "https://makaosafe-backend.onrender.com"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Added 'Origin' and 'X-Requested-With' which are common causes for 403 in browser
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}