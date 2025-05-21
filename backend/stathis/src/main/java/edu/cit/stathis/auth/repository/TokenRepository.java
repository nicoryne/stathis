package edu.cit.stathis.auth.repository;

import edu.cit.stathis.auth.entity.Token;
import edu.cit.stathis.auth.entity.User;
import edu.cit.stathis.auth.enums.TokenTypeEnum;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

  // Find
  Optional<Token> findByTokenHash(String tokenHash);

  Optional<Token> findByUser_UserIdAndTokenType(UUID userId, TokenTypeEnum tokenType);

  // Exist
  boolean existsByTokenHashAndRevokedFalse(String tokenHash);

  List<Token> findAllByUserAndTokenTypeAndRevokedFalse(User user, TokenTypeEnum tokenType);

  List<Token> findAllByTokenTypeAndRevokedFalseAndExpiresAtAfter(
      TokenTypeEnum tokenType, OffsetDateTime now);
}
