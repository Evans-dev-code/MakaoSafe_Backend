package com.example.makaosafe.service;

import com.example.makaosafe.dto.AuthResponse;
import com.example.makaosafe.dto.LoginRequest;
import com.example.makaosafe.dto.RegisterRequest;
import com.example.makaosafe.entity.User;
import com.example.makaosafe.repository.UserRepository;
import com.example.makaosafe.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .isVerified(false)
                .build();

        userRepository.save(user);

        String subject = "Welcome to MakaoSafe!";
        String body = "Hello " + user.getFullName() + ",\n\n" +
                "Welcome to MakaoSafe! Your account has been successfully created.\n" +
                "You can now login and explore properties.\n\n" +
                "Regards,\nMakaoSafe Team";

        emailService.sendEmail(user.getEmail(), subject, body);

        var jwtToken = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}