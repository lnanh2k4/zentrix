package com.zentrix.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date April 01, 2025
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Value("${cors.allowed-origins}")
        private List<String> allowedOrigin;

        @Value("${cors.allowed-methods}")
        private List<String> allowMethods;

        @Value("${cors.allowed-headers}")
        private List<String> allowedHeaders;

        @Value("${cors.allowed-credentials}")
        private boolean allowedCredentials;

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        private static final String[] GUEST_ENDPOINT = {
                        "/api/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/uploads/**",
                        "/api/v1/posts", "/api/v1/posts/{postId}",
                        "/api/v1/reviews", "/api/v1/reviews/{reviewId}",
                        "/api/v1/notifications", "/api/v1/products/homepage",
                        "/api/v1/categories", "/api/v1/products/ImageProduct/{prodTypeId}",
                        "/api/v1/products/productTypeBranchs", "/api/v1/products/productTypes/{id}",
                        "/api/v1/auth/info", "/api/v1/products/productTypeVariation/{id}",
                        "/api/v1/products/productTypeAttribute/{id}", "api/v1/notifications/{notificationId}",
                        "/api/v1/branches", "/api/v1/branches/{id}", "/api/v1/branches/all",
                        "/api/v1/branches/search/{name}", "/api/v1/categories", "/api/v1/categories/{id}",
                        "/api/v1/categories/sub/{parentId}", "/api/v1/suppliers", "/api/v1/suppliers/{id}",
                        "/api/v1/suppliers/search?name={name}", "/api/v1/notifications/get-all/{id}",
                        "/api/v1/products/productTypeBranch/{prodTypeId}", "/api/v1/branches/{branchId}/product-types",
                        "/api/v1/reviews/public/{productId}", "/api/v1/products**",
                        "/api/v1/products/product/searchBy/{id}", "/api/v1/products/productTypeAttributes"
        };

        private static final String[] CUSTOMER_ENDPOINT = {
                        "/api/v1/reviews/add", "/api/v1/reviews", "/api/v1/reviews/{id}",
                        "/api/v1/promotions/claim", "/api/v1/promotions/my-promotions", "/api/v1/promotions/{id}",
                        "/api/v1/promotions/filter", "/api/v1/cart/**", "/api/v1/memberships", "/api/v1/orders/**",
                        "/api/v1/posts", "/api/v1/posts/{id}", "/api/v1/notifications", "/api/v1/notifications/{id}",
                        "/api/v1/reviews/check-condition", "/api/v1/users", "/api/v1/auth/change-password",
                        "/api/v1/staffs/**", "/api/v1/users/**",
                        "/api/v1/reviews/public/{productId}"
        };

        private static final String[] SHIPPER_STAFF_ENDPOINT = {
                        "/api/v1/posts", "/api/v1/posts/{id}", "/api/v1/reviews", "/api/v1/reviews/{id}",
                        "/api/v1/notifications", "/api/v1/notifications/{id}", "/api/v1/reviews/check-condition",
                        "/api/v1/staffs/username/{username}", "/api/v1/users/roles", "/api/v1/branches",
                        "/api/v1/reviews/public/{productId}"
        };

        private static final String[] SELLER_STAFF_ENDPOINT = {
                        "/api/v1/posts", "/api/v1/posts/{id}", "/api/v1/dashboard/posts/**",
                        "/api/v1/dashboard/reviews/**", "/api/v1/dashboard/notifications/**", "/api/v1/warranties/**",
                        "/api/v1/notifications/{id}",
                        "/api/v1/promotions", "/api/v1/promotions/search", "/api/v1/promotions/list",
                        "/api/v1/promotions/{id}", "/api/v1/promotions/**", "/api/v1/reviews/check-condition",
                        "/api/v1/staffs/username/{username}", "/api/v1/users/roles", "/api/v1/branches/all",
                        "/api/v1/reviews/public/{productId}"

        };

        private static final String[] WAREHOUSE_STAFF_ENDPOINT = {
                        "/api/v1/posts", "/api/v1/posts/{id}", "/api/v1/reviews", "/api/v1/reviews/{id}",
                        "/api/v1/notifications", "/api/v1/stocks/**", "/api/v1/notifications/{id}",
                        "/api/v1/suppliers", "/api/v1/branches", "/api/v1/attributes/**", "/api/v1/variations/**",
                        "/api/v1/products/**", "/api/v1/reviews/check-condition",
                        "/api/v1/staffs/username/{username}", "/api/v1/users/roles",
                        "/api/v1/reviews/public/{productId}"

        };

        private static final String[] ADMIN_ENDPOINT = {
                        "/api/v1/users/**", "/api/v1/staffs/**", "/api/v1/warranties/**", "/api/v1/memberships/**",
                        "/api/v1/dashboard/posts/**", "/api/v1/posts/**", "/api/v1/dashboard/reviews/**",
                        "/api/v1/dashboard/notifications/**", "/api/v1/dashboard/categories",
                        "/api/v1/dashboard/categories/{id}", "/api/v1/dashboard/suppliers",
                        "/api/v1/dashboard/suppliers/{id}",
                        "/api/v1/branches",
                        "/api/v1/branches/{id}",
                        "/api/v1/branches/{branchId}/product-types", "/api/v1/users/roles",
                        "/api/v1/reviews/public/{productId}"

        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(GUEST_ENDPOINT).permitAll()
                                                .requestMatchers(CUSTOMER_ENDPOINT)
                                                .hasAnyRole("CUSTOMER", "SHIPPER", "WAREHOUSE STAFF", "SELLER STAFF",
                                                                "ADMIN")
                                                .requestMatchers(SHIPPER_STAFF_ENDPOINT)
                                                .hasAnyRole("SHIPPER", "ADMIN")
                                                .requestMatchers(SELLER_STAFF_ENDPOINT)
                                                .hasAnyRole("SELLER STAFF", "ADMIN")
                                                .requestMatchers(WAREHOUSE_STAFF_ENDPOINT)
                                                .hasAnyRole("WAREHOUSE STAFF", "ADMIN")
                                                .requestMatchers(ADMIN_ENDPOINT).hasAnyRole("ADMIN")
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("http://localhost:5173/login")
                                                .defaultSuccessUrl("/api/v1/auth/google/callback", true))
                                .headers(headers -> headers.addHeaderWriter(new StaticHeadersWriter(
                                                "Referrer-Policy", "strict-origin-when-cross-origin")))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(allowedOrigin);
                configuration.setAllowedMethods(allowMethods);
                configuration.setAllowedHeaders(allowedHeaders);
                configuration.setAllowCredentials(allowedCredentials);
                configuration.setMaxAge(3600L);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public RestTemplate restTemplate() {
                return new RestTemplate();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
