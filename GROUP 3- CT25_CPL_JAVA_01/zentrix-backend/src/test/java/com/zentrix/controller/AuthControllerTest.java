// package com.zentrix.controller;

// import java.time.LocalDate;
// import java.util.Date;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import
// org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;

// import com.zentrix.model.entity.Membership;
// import com.zentrix.model.request.LoginRequest;
// import com.zentrix.model.request.UserRequest;
// import com.zentrix.model.response.AuthResponse;
// import com.zentrix.model.utils.Status;
// import com.zentrix.service.UserService;

// @SpringBootTest
// @AutoConfigureMockMvc
// public class AuthControllerTest {

// @Autowired
// private MockMvc mockMvc;

// @MockitoBean
// private UserService userService;

// private AuthResponse response;

// private LoginRequest loginRequest;

// private UserRequest userRequest;

// @Autowired
// private PasswordEncoder passwordEncoder;

// @BeforeEach
// void initData() {
// userRequest = new UserRequest();
// userRequest.setUsername("lnanh2k4");
// userRequest.setPassword(passwordEncoder.encode("lnanh2k4"));
// userRequest.setEmail("lnanh2k4@gmail.com");
// userRequest.setFirstName("Nhut Anh");
// userRequest.setLastName("Le");
// userRequest.setAddress("Soctrang city");
// userRequest.setCompanyName("");
// userRequest.setTaxCode("");
// userRequest.setDob(LocalDate.of(2004, 03, 14));
// userRequest.setMbsId(null);
// userRequest.setPhone("0393510720");
// userRequest.setSex(1);
// }

// @Test
// void testSignUp() {

// }

// @Test
// void testSignIn() {

// }
// }
