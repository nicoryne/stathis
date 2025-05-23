package edu.cit.stathis.auth.repository;

import edu.cit.stathis.auth.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

  // Find
  Optional<UserProfile> findByUser_UserId(UUID userId);
  Optional<UserProfile> findByUser_PhysicalId(String physicalId);

}
