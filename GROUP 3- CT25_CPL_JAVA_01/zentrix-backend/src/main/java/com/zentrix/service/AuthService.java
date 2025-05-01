package com.zentrix.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.utils.JWTUtil;
import com.zentrix.model.utils.Role;
import com.zentrix.model.utils.Sex;
import com.zentrix.model.utils.Status;
import com.zentrix.repository.RoleRepository;
import com.zentrix.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JWTUtil jwtUtil;
    private final RoleRepository roleRepository;

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public String handleGoogleLogin(String idToken) {
        String googleUserInfoUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" + idToken;
        ResponseEntity<Map> response = restTemplate.getForEntity(googleUserInfoUrl, Map.class);
        Map<String, Object> userInfo = response.getBody();
        if (userInfo == null || !userInfo.containsKey("email")) {
            throw new ValidationFailedException(AppCode.GOOGLE_TOKEN_INVALID);
        }
        String email = (String) userInfo.get("email");
        String fullName = (String) userInfo.get("name");
        String firstName;
        String lastName;
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] nameParts = fullName.trim().split("\\s+");
            firstName = nameParts[0]; // Always safe to access first element
            lastName = nameParts.length > 1 ? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length)) : "";
        } else {
            // Fallback if name is null or empty
            firstName = email != null ? email.split("@")[0] : "Unknown";
            lastName = "";
        }
        User user = userRepository.findByEmail(email).orElseGet(
                () -> {
                    User newUser = User.builder()
                            .email(email)
                            .username(email.split("@")[0] + "-" + UUID.randomUUID().toString())
                            .firstName(firstName)
                            .lastName(lastName)
                            .password(passwordEncoder.encode("G0og!e-@u!h"))
                            .status(Status.ACTIVE.getValue())
                            .sex(Sex.UNKNOWN.getValue())
                            .dob(LocalDate.now())
                            .roleId(roleRepository.findByRoleName(Role.CUSTOMER.getRoleName()))
                            .phone("google-" + UUID.randomUUID().toString())
                            .build();
                    return userRepository.save(newUser);
                });
        if (user != null) {
            if (user.getStatus() == Status.VERIFYING.getValue()) {
                user.setStatus(Status.ACTIVE.getValue());
                userRepository.save(user);
            }
        }
        // Tạo UserDetails từ User
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRoleId().getRoleName())));

        // Thiết lập Authentication vào SecurityContextHolder
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null, // Không cần credentials vì dùng Google login
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtil.generateToken(user.getEmail());
    }
}
