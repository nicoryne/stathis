package edu.cit.stathis.common.utils;

import edu.cit.stathis.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Long expiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_" + user.getUserRole().toString());
    return createToken(claims, user.getEmail());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  public String extractUsername(String token) {
        try {
    return extractClaim(token, Claims::getSubject);
        } catch (JwtException e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            throw e;
        }
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
        } catch (JwtException e) {
            logger.error("Error extracting claim from token: {}", e.getMessage());
            throw e;
        }
  }

  private Claims extractAllClaims(String token) {
        try {
    JwtParser parser = Jwts.parser().verifyWith(getSigningKey()).build();
            return parser.parseSignedClaims(token).getPayload();
        } catch (JwtException e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            throw e;
        }
  }

  public Boolean validateToken(String token, String username) {
        try {
    final String extractedUsername = extractUsername(token);
            boolean isValid = extractedUsername.equals(username) && !isTokenExpired(token);
            if (!isValid) {
                logger.debug("Token validation failed for user: {}", username);
            }
            return isValid;
        } catch (JwtException e) {
            logger.error("Error validating token: {}", e.getMessage());
            return false;
        }
  }

  private Boolean isTokenExpired(String token) {
        try {
    return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (JwtException e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
  }
}
