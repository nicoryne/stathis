package edu.cit.stathis.auth.repository;

import edu.cit.stathis.auth.entity.User;
import edu.cit.stathis.auth.enums.UserRoleEnum;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByUserId(UUID userId);

  Optional<User> findByEmail(String email);

  @Query("SELECT u.userRole FROM User u WHERE u.userId = :userId")
  Optional<UserRoleEnum> findUserRoleByUserId(UUID userId);

  boolean existsByEmail(String email);

  boolean existsByPhysicalId(String physicalId);
}
