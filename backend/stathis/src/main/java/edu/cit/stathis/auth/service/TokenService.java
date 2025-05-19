package edu.cit.stathis.auth.service;

import edu.cit.stathis.auth.entity.Token;
import edu.cit.stathis.auth.entity.User;
import edu.cit.stathis.auth.enums.TokenTypeEnum;
import edu.cit.stathis.auth.repository.TokenRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

  @Autowired private TokenRepository tokenRepo;

  @Autowired private PasswordEncoder passwordEncoder;

  public record CreatedToken(String rawToken, Token savedToken) {}

  @Transactional
  public CreatedToken createToken(User user, TokenTypeEnum tokenType, OffsetDateTime expiresAt) {
    String tokenValue = UUID.randomUUID().toString();
    String tokenHash = passwordEncoder.encode(tokenValue);

    Token token =
        Token.builder()
            .user(user)
            .tokenType(tokenType)
            .tokenHash(tokenHash)
            .expiresAt(expiresAt)
            .createdAt(OffsetDateTime.now())
            .build();

    tokenRepo.save(token);
    return new CreatedToken(tokenValue, token);
  }

  public CreatedToken createRefreshToken(User user) {
    OffsetDateTime expiry = OffsetDateTime.now().plusDays(7);
    return createToken(user, TokenTypeEnum.REFRESH, expiry);
  }

  public boolean validateToken(String tokenValue, TokenTypeEnum expectedType) {
    return tokenRepo
        .findByTokenHash(tokenValue)
        .map(
            token ->
                passwordEncoder.matches(tokenValue, token.getTokenHash())
                    && !token.isRevoked()
                    && !token.getExpiresAt().isBefore(OffsetDateTime.now())
                    && token.getTokenType() == expectedType)
        .orElse(false);
  }

  @Transactional
  public void revokeToken(String tokenValue) {
    tokenRepo
        .findByTokenHash(tokenValue)
        .ifPresent(
            token -> {
              token.setRevoked(true);
              tokenRepo.save(token);
            });
  }

  @Transactional
  public void revokeAllTokensForUser(User user, TokenTypeEnum type) {
    tokenRepo
        .findAllByUserAndTokenTypeAndRevokedFalse(user, type)
        .forEach(
            token -> {
              token.setRevoked(true);
              tokenRepo.save(token);
            });
  }
}
