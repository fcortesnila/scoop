package com.scoop.api.service;

import com.scoop.api.dto.ApiDtos.AuthResponse;
import com.scoop.api.dto.ApiDtos.UserResponse;
import com.scoop.api.entity.AppUser;
import com.scoop.api.entity.UserRole;
import com.scoop.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtDecoder jwtDecoder;
    private final Set<String> operatorEmails;
    private final boolean demoEnabled;

    public AuthService(UserRepository userRepository, JwtDecoder jwtDecoder,
                       @Value("${app.operator-emails:}") String operatorEmails,
                       @Value("${app.demo-login-enabled:false}") boolean demoEnabled) {
        this.userRepository = userRepository;
        this.jwtDecoder = jwtDecoder;
        this.operatorEmails = Arrays.stream(operatorEmails.split(","))
                .map(String::trim).map(email -> email.toLowerCase(Locale.ROOT))
                .filter(email -> !email.isBlank()).collect(Collectors.toUnmodifiableSet());
        this.demoEnabled = demoEnabled;
    }

    @Transactional
    public AuthResponse googleLogin(String credential) {
        Jwt jwt = jwtDecoder.decode(credential);
        String sub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        if (sub == null || email == null || !Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
            throw new IllegalArgumentException("Google account must have a verified email");
        }
        UserRole role = operatorEmails.contains(email.toLowerCase(Locale.ROOT))
                ? UserRole.OPERATOR : UserRole.CUSTOMER;
        AppUser user = userRepository.findByGoogleSub(sub).orElseGet(AppUser::new);
        user.setGoogleSub(sub);
        user.setEmail(email);
        user.setName(name == null || name.isBlank() ? email : name);
        user.setRole(role);
        user = userRepository.save(user);
        return new AuthResponse(credential, new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }

    public AuthResponse demoLogin() {
        if (!demoEnabled) throw new IllegalStateException("Demo login is disabled");
        return new AuthResponse("demo-operator", new UserResponse(null, "Demo Operator",
                "operator@example.com", UserRole.OPERATOR));
    }
}
