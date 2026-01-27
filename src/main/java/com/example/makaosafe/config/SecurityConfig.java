package com.example.makaosafe.config;

import com.example.makaosafe.security.CustomUserDetailsService;
import com.example.makaosafe.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. CORS Pre-flight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Public Auth & Callbacks
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/payment/callback/**").permitAll()

                        // 3. Public Property Viewing
                        .requestMatchers(HttpMethod.GET, "/api/properties").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/properties/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/properties/{id:[0-9]+}").permitAll()

                        // 4. Secure Contact (WhatsApp) - Requires Authentication
                        .requestMatchers(HttpMethod.GET, "/api/properties/*/contact").authenticated()

                        // 5. Payments & Bookings - Restricted to valid roles
                        .requestMatchers("/api/payment/pay/**").hasAnyAuthority("ROLE_TENANT", "TENANT", "ROLE_LANDLORD", "LANDLORD")
                        .requestMatchers("/api/bookings/my-bookings").authenticated()
                        .requestMatchers("/api/bookings/**").authenticated()
                        .requestMatchers("/api/payment/**").authenticated()

                        // 6. Landlord Listings Management
                        .requestMatchers("/api/properties/my-listings").hasAnyAuthority("ROLE_LANDLORD", "LANDLORD")
                        .requestMatchers(HttpMethod.POST, "/api/properties/**").hasAnyAuthority("ROLE_LANDLORD", "LANDLORD")

                        .anyRequest().authenticated()
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
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "https://makao-safe-frontend.vercel.app",
                "https://makao-safe.vercel.app",
                "https://makaosafe-backend.onrender.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}