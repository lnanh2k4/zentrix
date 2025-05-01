package com.zentrix.model.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.zentrix.model.entity.User;
import com.zentrix.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  March 26, 2025
 */

@Component
public class JWTUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Autowired
    private UserService userService;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * This method allows to generate token
     * 
     * @param userDetails user details
     * @return jwt token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * This method allows to create token
     * 
     * @param claims  information with subject and value
     * @param subject username of account
     * @return token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * This method allows to validate token
     * 
     * @param token       jwt token
     * @param userDetails user details
     * @return true if token is validated successfully and vice versa
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * This method allows to extract username from token
     * 
     * @param token jwt token
     * @return username of user
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * This method allows to extract claims from token
     * 
     * @param <T>            Type of data
     * @param token          jwt token
     * @param claimsResolver function to handle claim that is extracted claim
     *                       successfully!
     * @return
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * This method allows to extract all claims from token
     * 
     * @param token jwt token
     * @return value of claim that is extracted successfully
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * This method allows to check token is expired or not
     * 
     * @param token jwt token
     * @return true if token is expired and vice versa
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public String generateToken(String email) {
        User user = userService.findUserByEmail(email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoleId().getRoleName());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    private Claims getClaimsFromToken(String token) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Sử dụng JwtParserBuilder
                .build();
        return parser.parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token); // Nếu parse thành công thì token hợp lệ
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
