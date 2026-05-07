package com.mksafenet.service;

import com.mksafenet.dto.LoginRequest;
import com.mksafenet.dto.LoginResponse;
import com.mksafenet.model.User;
import com.mksafenet.repository.UserRepository;
import com.mksafenet.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return LoginResponse.builder()
            .token(token)
            .role(user.getRole().name())
            .username(user.getUsername())
            .displayName(user.getDisplayName())
            .schoolId(user.getSchool() != null ? user.getSchool().getId() : null)
            .schoolName(user.getSchool() != null ? user.getSchool().getName() : null)
            .build();
    }
}
