package com.zentrix.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.request.ChangePasswordRequest;
import com.zentrix.model.request.ForgotPasswordRequest;
import com.zentrix.model.request.LoginRequest;
import com.zentrix.model.request.ResetPasswordRequest;
import com.zentrix.model.request.UserRequest;
import com.zentrix.model.response.AuthResponse;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.model.utils.JWTUtil;

import com.zentrix.model.utils.Role;
import com.zentrix.model.utils.Status;
import com.zentrix.service.AuthService;
import com.zentrix.service.RoleService;
import com.zentrix.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/

@RestController
@RequestMapping("api/v1/auth")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Authentication Controller", description = "This class supports authentication API")
public class AuthController {
        @Autowired
        UserService userService;
        @Autowired
        PasswordEncoder passwordEncoder;
        @Autowired
        RoleService roleService;
        @Autowired
        JWTUtil jwtUtil;
        @Autowired
        AuthenticationManager authenticationManager;
        @Autowired
        UserDetailsService userDetailsService;
        @Autowired
        AuthService authService;

        /**
         * This API allows to get all information of customer
         * 
         * @param userDetails userdetails
         * @return information of customer
         */
        @GetMapping("/info")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get Info", description = "This API allows to get all information of customer")
        @PreAuthorize("permitAll()")
        public ResponseEntity<ResponseObject<User>> getInfo(@AuthenticationPrincipal UserDetails userDetails) {
                if (userDetails == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseObject.Builder<User>()
                                        .code(HttpStatus.UNAUTHORIZED.value()).message("Not authenticated").build());
                }
                String username = userDetails.getUsername();

                User user = userService.findUserByUsername(username);
                if (user == null) {
                        throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
                }
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject.Builder<User>().success(true)
                                .code(HttpStatus.OK.value()).message("Get infor sucessfully").content(user).build());
        }

        @GetMapping("/google/callback")
        public ResponseEntity<ResponseObject<String>> googleCallback() {
                return ResponseEntity.status(HttpStatus.FOUND).header("Location", "http://localhost:5173/")
                                .body(new ResponseObject.Builder<String>()
                                                .code(HttpStatus.OK.value())
                                                .content(null)
                                                .message("Log in with google successfully")
                                                .success(true)
                                                .build());
        }

        /**
         * This method allows to sign up a new account
         *
         * @param UserRequest information of user
         * @return Authentication Response
         */
        @PostMapping("/sign-up")
        @Operation(summary = "Sign up", description = "This method allows to create a new user", method = "POST", operationId = "1")
        public ResponseEntity<ResponseObject<User>> signUp(@RequestBody UserRequest request) {

                User user = userService.createUser(User.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .email(request.getEmail().toLowerCase())
                                .dob(request.getDob())
                                .phone(request.getPhone())
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .address(request.getAddress())
                                .sex(request.getSex())
                                .companyName(request.getCompanyName())
                                .taxCode(request.getTaxCode())
                                .roleId(roleService.findRoleByName(Role.CUSTOMER.getRoleName()))
                                .status(Status.VERIFYING.getValue())
                                .build());

                ResponseObject<User> responseObject = new ResponseObject.Builder<User>()
                                .content(user)
                                .message("Sign up account successfully")
                                .code(HttpStatus.CREATED.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED.value()).body(responseObject);
        }

        /**
         * This method allows to sign in account
         *
         * @param request  username and password to login
         * @param response Http Servlet Response
         * @return JWT
         */
        @PostMapping("/sign-in")
        @Operation(summary = "Sign in", description = "This method allows to login account with username and password and response token", method = "POST", operationId = "2")

        public ResponseEntity<ResponseObject<AuthResponse>> signIn(@RequestBody LoginRequest request,
                        HttpServletResponse response) {
                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.getUsername(),
                                                        request.getPassword()));
                        System.out.println(authentication);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
                        String token = jwtUtil.generateToken(userDetails);

                        Cookie jwtCookie = new Cookie("jwt", token);
                        jwtCookie.setHttpOnly(true);
                        jwtCookie.setSecure(false);
                        jwtCookie.setPath("/");
                        jwtCookie.setMaxAge(24 * 60 * 60);
                        response.addCookie(jwtCookie);
                        ResponseObject<AuthResponse> responseObject = new ResponseObject.Builder<AuthResponse>()
                                        .message("Sign in account successfully")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.status(HttpStatus.OK.value()).body(responseObject);

                } catch (Exception e) {
                        ResponseObject<AuthResponse> responseObject = new ResponseObject.Builder<AuthResponse>()
                                        .message("Invalid credentials: " + e.getMessage())
                                        .code(HttpStatus.UNAUTHORIZED.value())
                                        .success(false)
                                        .build();
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(responseObject);
                }
        }

        /**
         * This API allows to sign out account
         * 
         * @param response Http Servlet Response
         * @return a string
         */
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Sign Out", description = "This API allows to sign out account")
        @PostMapping("/sign-out")
        public ResponseEntity<ResponseObject<String>> signOut(HttpServletResponse response) {
                SecurityContextHolder.getContext().setAuthentication(null);
                Cookie jwtCookie = new Cookie("jwt", null);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(false);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(0);
                response.addCookie(jwtCookie);
                ResponseObject<String> responseObject = new ResponseObject.Builder<String>()
                                .message("Logout")
                                .code(HttpStatus.OK.value())
                                .content("Logout successfully!")
                                .success(true)
                                .build();
                return ResponseEntity.ok(responseObject);
        }

        @PostMapping("/google")
        public ResponseEntity<ResponseObject<String>> googleLogin(@RequestBody Map<String, String> payload) {
                String idToken = payload.get("idToken");
                String jwt = authService.handleGoogleLogin(idToken);
                ResponseObject<String> responseObject = new ResponseObject.Builder<String>()
                                .code(HttpStatus.OK.value())
                                .content(jwt)
                                .message("Log in with google successfully")
                                .success(true)
                                .build();
                return ResponseEntity.ok(responseObject);
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<ResponseObject<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
                userService.verifyRequest(request.getIdentifier(), request.getMethod(), "reset");
                ResponseObject<String> response = new ResponseObject.Builder<String>()
                                .success(true)
                                .code(HttpStatus.OK.value())
                                .message("Reset request sent successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        @PostMapping("/reset-password")
        public ResponseEntity<ResponseObject<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
                if (request.getToken() != null) {
                        userService.resetPasswordWithToken(request.getToken(),
                                        passwordEncoder.encode(request.getNewPassword()));
                } else if (request.getOtp() != null) {
                        userService.resetPasswordWithOtp(request.getOtp(),
                                        passwordEncoder.encode(request.getNewPassword()));
                }

                ResponseObject<String> response = new ResponseObject.Builder<String>()
                                .success(true)
                                .code(HttpStatus.OK.value())
                                .message("Password reset successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        @PostMapping("/verify-email")
        public ResponseEntity<ResponseObject<String>> verifyEmail(@RequestParam("email") String email) {
                userService.verifyRequest(email, "email", "verify");
                ResponseObject<String> response = new ResponseObject.Builder<String>()
                                .success(true)
                                .code(HttpStatus.OK.value())
                                .message("Request verify email successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        @PostMapping("/verify-email/{token}")
        public ResponseEntity<ResponseObject<String>> verifyToken(@PathVariable String token) {
                userService.verifyWithToken(token);
                ResponseObject<String> response = new ResponseObject.Builder<String>()
                                .success(true)
                                .code(HttpStatus.OK.value())
                                .message("Request verify email successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        @PatchMapping("/password/change/{userId}")
        @PreAuthorize("permitAll()")
        public ResponseEntity<ResponseObject<User>> changePassword(@PathVariable Long userId,
                        @RequestBody ChangePasswordRequest request) {
                System.out.println("Current password " + request.getCurrentPassword());
                System.out.println("New password " + request.getNewPassword());
                User user = userService.findUserByUserId(userId);
                if (user == null) {
                        throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
                }
                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        throw new ActionFailedException(AppCode.CURRENT_PASSWORD_INCORRECT);
                }
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                ResponseObject<User> response = new ResponseObject.Builder<User>().content(userService.updateUser(user))
                                .success(true)
                                .code(HttpStatus.OK.value()).message("Change password successfully").build();
                return ResponseEntity.ok().body(response);
        }

        @PatchMapping("/password/reset/{userId}")
        public ResponseEntity<ResponseObject<User>> resetPassword(@PathVariable("userId") Long userId,
                        @RequestParam String newPassword) {
                User user = userService.findUserByUserId(userId);
                if (user == null) {
                        throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
                }
                user.setPassword(passwordEncoder.encode(newPassword));

                ResponseObject<User> response = new ResponseObject.Builder<User>().content(userService.updateUser(user))
                                .success(true)
                                .code(HttpStatus.OK.value()).message("Change password successfully").build();
                return ResponseEntity.ok().body(response);
        }

}
