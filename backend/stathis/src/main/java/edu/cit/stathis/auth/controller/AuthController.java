package edu.cit.stathis.auth.controller;

import edu.cit.stathis.auth.dto.AuthResponseDTO;
import edu.cit.stathis.auth.dto.CreateUserDTO;
import edu.cit.stathis.auth.dto.LoginDTO;
import edu.cit.stathis.auth.dto.UserResponseDTO;
import edu.cit.stathis.auth.entity.Token;
import edu.cit.stathis.auth.entity.User;
import edu.cit.stathis.auth.enums.TokenTypeEnum;
import edu.cit.stathis.auth.enums.UserRoleEnum;
import edu.cit.stathis.auth.repository.TokenRepository;
import edu.cit.stathis.auth.service.TokenService;
import edu.cit.stathis.auth.service.UserService;
import edu.cit.stathis.common.utils.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints related to user authentication")
@CrossOrigin
public class AuthController {

  @Autowired private UserService userService;
  @Autowired private TokenService tokenService;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private TokenRepository tokenRepo;

  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Congratulations!");
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO) {
    return ResponseEntity.ok(userService.loginAndGenerateTokens(loginDTO));
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponseDTO> registerUser(@RequestBody CreateUserDTO userDTO) {
    User user = userService.createUser(userDTO, UserRoleEnum.STUDENT);
    UserResponseDTO response = userService.buildUserResponse(user);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/verify-email")
  public ResponseEntity<String> verifyEmail(@RequestParam("token") String tokenValue) {
    userService.verifyEmail(tokenValue);
    return new ResponseEntity<>("Email verified successfully!", HttpStatus.OK);
  }

  @PostMapping("/resend-verification-email")
  public ResponseEntity<?> resendVerification(@RequestParam String email) {
    try {
      String message = userService.resendVerificationEmail(email);
      return ResponseEntity.ok(message);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
    Optional<Token> tokenOpt = tokenRepo.findByTokenHash(refreshToken);
    if (tokenOpt.isEmpty() || !tokenService.validateToken(refreshToken, TokenTypeEnum.REFRESH)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Token oldToken = tokenOpt.get();
    User user = oldToken.getUser();
    tokenService.revokeToken(refreshToken);

    String newAccessToken = jwtUtil.generateToken(user);
    TokenService.CreatedToken newRefresh = tokenService.createRefreshToken(user);

    return ResponseEntity.ok(
        AuthResponseDTO.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefresh.rawToken())
            .build());
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestParam String refreshToken) {
    tokenService.revokeToken(refreshToken);
    return ResponseEntity.ok("Logged out");
  }
}
