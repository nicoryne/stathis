package edu.cit.stathis.auth.service;

import edu.cit.stathis.auth.dto.AuthResponseDTO;
import edu.cit.stathis.auth.dto.CreateUserDTO;
import edu.cit.stathis.auth.dto.LoginDTO;
import edu.cit.stathis.auth.dto.UpdateStudentProfileDTO;
import edu.cit.stathis.auth.dto.UpdateTeacherProfileDTO;
import edu.cit.stathis.auth.dto.UpdateUserProfileDTO;
import edu.cit.stathis.auth.dto.UserResponseDTO;
import edu.cit.stathis.auth.entity.Token;
import edu.cit.stathis.auth.entity.User;
import edu.cit.stathis.auth.entity.UserProfile;
import edu.cit.stathis.auth.enums.TokenTypeEnum;
import edu.cit.stathis.auth.enums.UserRoleEnum;
import edu.cit.stathis.auth.repository.TokenRepository;
import edu.cit.stathis.auth.repository.UserProfileRepository;
import edu.cit.stathis.auth.repository.UserRepository;
import edu.cit.stathis.auth.service.TokenService.CreatedToken;
import edu.cit.stathis.common.utils.JwtUtil;
import jakarta.mail.MessagingException;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  @Autowired private UserRepository uRepo;

  @Autowired private UserProfileRepository upRepo;

  @Autowired private TokenRepository tokenRepo;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private EmailService emailService;

  @Autowired private TokenService tokenService;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private WebhookService webhookService;

  @Transactional
  public User createUser(CreateUserDTO userDTO, UserRoleEnum role) {
    if (existByEmail(userDTO.getEmail())) {
      throw new IllegalArgumentException("Email is already in use.");
    }

    User user = new User();
    user.setEmail(userDTO.getEmail());
    user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
    user.setUserRole(role);
    user.setPhysicalId(provideUniquePhysicalId());
    user.setEmailVerified(false);

    UserProfile userProfile = new UserProfile();
    userProfile.setUser(user);
    userProfile.setFirstName(userDTO.getFirstName());
    userProfile.setLastName(userDTO.getLastName());

    user = uRepo.save(user);
    upRepo.save(userProfile);

    CreatedToken created =
        tokenService.createToken(
            user, TokenTypeEnum.EMAIL_VERIFICATION, OffsetDateTime.now().plusMinutes(30));

    String tokenValue = created.rawToken();

    try {
      emailService.sendVerificationEmail(user.getEmail(), tokenValue);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send verification email", e);
    }

    webhookService.notifyUserEvent(user, "registered");

    return user;
  }

  public String resendVerificationEmail(String email) {
    User user =
        uRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.isEmailVerified()) {
      throw new IllegalStateException("User is already verified");
    }

    tokenService.revokeAllTokensForUser(user, TokenTypeEnum.EMAIL_VERIFICATION);

    CreatedToken created =
        tokenService.createToken(
            user, TokenTypeEnum.EMAIL_VERIFICATION, OffsetDateTime.now().plusMinutes(30));

    String tokenValue = created.rawToken();

    try {
      emailService.sendVerificationEmail(user.getEmail(), tokenValue);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send verification email", e);
    }

    webhookService.notifyUserEvent(user, "registered");

    return "Verification email has been resent.";
  }

  @Transactional
  public AuthResponseDTO loginAndGenerateTokens(LoginDTO loginDTO) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    User user =
        uRepo
            .findByEmail(loginDTO.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!user.isEmailVerified()) {
      throw new IllegalArgumentException("Email not verified.");
    }

    tokenService.revokeAllTokensForUser(user, TokenTypeEnum.REFRESH);

    String accessToken = jwtUtil.generateToken(user);
    CreatedToken refresh = tokenService.createRefreshToken(user);
    String tokenValue = refresh.rawToken();
    OffsetDateTime expiresAt = refresh.expiresAt();

    return AuthResponseDTO.builder()
        .accessToken(accessToken)
        .refreshToken(tokenValue)
        .expiresAt(expiresAt)
        .build();
  }

  @Transactional
  public AuthResponseDTO refreshToken(String tokenValue) {
    Token token =
        tokenService
            .getValidToken(tokenValue, TokenTypeEnum.REFRESH)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token."));

    if (token.getUsedAt() != null) {
      throw new IllegalArgumentException("Token already used.");
    }

    User user = token.getUser();

    tokenService.revokeAllTokensForUser(user, TokenTypeEnum.REFRESH);

    String accessToken = jwtUtil.generateToken(user);
    CreatedToken refresh = tokenService.createRefreshToken(user);
    String newRefreshToken = refresh.rawToken();
    OffsetDateTime expiresAt = refresh.expiresAt();

    return AuthResponseDTO.builder()
        .accessToken(accessToken)
        .refreshToken(newRefreshToken)
        .expiresAt(expiresAt)
        .build();
  }

  @Transactional
  public void forgotPassword(String email) {
    User user =
        uRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));

    CreatedToken created =
        tokenService.createToken(
            user, TokenTypeEnum.PASSWORD_RESET, OffsetDateTime.now().plusMinutes(30));

    String tokenValue = created.rawToken();

    try {
      emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send password reset email", e);
    }
  }

  @Transactional
  public void resetPassword(String tokenValue, String newPassword) {
    Token token =
        tokenRepo
            .findByTokenHash(tokenValue)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token."));
    if (token.isRevoked()
        || token.getExpiresAt().isBefore(OffsetDateTime.now())
        || token.getUsedAt() != null) {
      throw new IllegalArgumentException("Invalid or expired token.");
    }
    if (token.getTokenType() != TokenTypeEnum.PASSWORD_RESET) {
      throw new IllegalArgumentException("Invalid token type.");
    }

    User user = token.getUser();
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    token.setUsedAt(OffsetDateTime.now());
    uRepo.save(user);
    tokenRepo.save(token);
  }

  @Transactional
  public void verifyEmail(String tokenValue) {
    Token token =
        tokenService
            .getValidToken(tokenValue, TokenTypeEnum.EMAIL_VERIFICATION)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token."));

    if (token.getUsedAt() != null) {
      throw new IllegalArgumentException("Token already used.");
    }

    User user = token.getUser();
    user.setEmailVerified(true);
    token.setUsedAt(OffsetDateTime.now());

    uRepo.save(user);
    tokenRepo.save(token);
  }

  public boolean existByEmail(String email) {
    return uRepo.existsByEmail(email);
  }

  public User findById(UUID userId) {
    return uRepo
        .findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found."));
  }

  public UserProfile findUserProfileByUserId(UUID userId) {
    return upRepo
        .findByUser_UserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("User profile not found."));
  }

  @Transactional
  public UserResponseDTO updateUserProfile(UUID userId, UpdateUserProfileDTO profileDTO) {
    User user = findById(userId);
    UserProfile userProfile = findUserProfileByUserId(userId);

    userProfile.setFirstName(profileDTO.getFirstName());
    userProfile.setLastName(profileDTO.getLastName());
    userProfile.setBirthdate(profileDTO.getBirthdate());
    userProfile.setProfilePictureUrl(profileDTO.getProfilePictureUrl());

    userProfile = upRepo.save(userProfile);

    webhookService.notifyUserEvent(user, "updated profile");

    return UserResponseDTO.builder()
        .physicalId(user.getPhysicalId())
        .email(user.getEmail())
        .firstName(userProfile.getFirstName())
        .lastName(userProfile.getLastName())
        .birthdate(userProfile.getBirthdate())
        .profilePictureUrl(userProfile.getProfilePictureUrl())
        .role(user.getUserRole())
        .school(userProfile.getSchool())
        .course(userProfile.getCourse())
        .yearLevel(userProfile.getYearLevel())
        .department(userProfile.getDepartment())
        .positionTitle(userProfile.getPositionTitle())
        .build();
  }

  @Transactional
  public UserResponseDTO updateStudentProfile(UUID userId, UpdateStudentProfileDTO studentDTO) {
    UserRoleEnum userRole =
        uRepo
            .findUserRoleByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
    if (userRole != UserRoleEnum.STUDENT) {
      throw new IllegalArgumentException("User must be a student to update student profile.");
    }

    User user = findById(userId);
    UserProfile userProfile = findUserProfileByUserId(userId);
    userProfile.setSchool(studentDTO.getSchool());
    userProfile.setCourse(studentDTO.getCourse());
    userProfile.setYearLevel(studentDTO.getYearLevel());

    userProfile = upRepo.save(userProfile);

    webhookService.notifyUserEvent(user, "updated student profile");

    return UserResponseDTO.builder()
        .physicalId(user.getPhysicalId())
        .email(user.getEmail())
        .firstName(userProfile.getFirstName())
        .lastName(userProfile.getLastName())
        .birthdate(userProfile.getBirthdate())
        .profilePictureUrl(userProfile.getProfilePictureUrl())
        .role(user.getUserRole())
        .school(userProfile.getSchool())
        .course(userProfile.getCourse())
        .yearLevel(userProfile.getYearLevel())
        .department(userProfile.getDepartment())
        .positionTitle(userProfile.getPositionTitle())
        .build();
  }

  @Transactional
  public UserResponseDTO updateTeacherProfile(UUID userId, UpdateTeacherProfileDTO teacherDTO) {
    UserRoleEnum userRole =
        uRepo
            .findUserRoleByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
    if (userRole != UserRoleEnum.TEACHER) {
      throw new IllegalArgumentException("User must be a teacher to update teacher profile.");
    }

    User user = findById(userId);
    UserProfile userProfile = findUserProfileByUserId(userId);
    userProfile.setSchool(teacherDTO.getSchool());
    userProfile.setDepartment(teacherDTO.getDepartment());
    userProfile.setPositionTitle(teacherDTO.getPositionTitle());

    userProfile = upRepo.save(userProfile);

    webhookService.notifyUserEvent(user, "updated teacher profile");

    return UserResponseDTO.builder()
        .physicalId(user.getPhysicalId())
        .email(user.getEmail())
        .firstName(userProfile.getFirstName())
        .lastName(userProfile.getLastName())
        .birthdate(userProfile.getBirthdate())
        .profilePictureUrl(userProfile.getProfilePictureUrl())
        .role(user.getUserRole())
        .school(userProfile.getSchool())
        .course(userProfile.getCourse())
        .yearLevel(userProfile.getYearLevel())
        .department(userProfile.getDepartment())
        .positionTitle(userProfile.getPositionTitle())
        .build();
  }

  @Transactional
  public boolean deleteUser(UUID userId) {
    User user = findById(userId);
    uRepo.delete(user);
    webhookService.notifyUserEvent(user, "deleted");
    return true;
  }

  public UserResponseDTO buildUserResponse(User user) {
    UserProfile profile = findUserProfileByUserId(user.getUserId());
    return UserResponseDTO.builder()
        .physicalId(user.getPhysicalId())
        .email(user.getEmail())
        .firstName(profile.getFirstName())
        .lastName(profile.getLastName())
        .birthdate(profile.getBirthdate())
        .profilePictureUrl(profile.getProfilePictureUrl())
        .role(user.getUserRole())
        .school(profile.getSchool())
        .course(profile.getCourse())
        .yearLevel(profile.getYearLevel())
        .department(profile.getDepartment())
        .positionTitle(profile.getPositionTitle())
        .build();
  }

  private String generatePhysicalId() {
    String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
    Random random = new Random();
    String secondPart = String.format("%04d", random.nextInt(10000));
    String thirdPart = String.format("%03d", random.nextInt(1000));
    return String.format("%s-%s-%s", year, secondPart, thirdPart);
  }

  private String provideUniquePhysicalId() {
    String generatedPhysicalId;
    do {
      generatedPhysicalId = generatePhysicalId();
    } while (uRepo.existsByPhysicalId(generatedPhysicalId));
    return generatedPhysicalId;
  }
}
