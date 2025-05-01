package com.zentrix.service;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.zentrix.model.entity.Role;
import com.zentrix.model.entity.User;
import com.zentrix.model.utils.JWTUtil;
import com.zentrix.repository.RoleRepository;
import com.zentrix.repository.UserRepository;

public class AuthServiceTest {
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private AuthService authService;
    private Map<String, Object> googleUserInfo;
    private Role customerRole;
    private User existingUser;

    @BeforeEach
    void setUp() {
        googleUserInfo = new HashMap<>();
        googleUserInfo.put("email", "zentrix.system@gmail.com");
        googleUserInfo.put("name", "Zentrix");

        customerRole = new Role();
        customerRole.setRoleName("CUSTOMER");

        existingUser = User.builder()
                .email("zentrix.system@gmail.com")
                .username("admin-zentrix.system")

                .build();
    }

    @Test
    void testHandleGoogleLogin() {

    }
}
