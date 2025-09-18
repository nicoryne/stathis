package edu.cit.stathis.auth.controller;

import edu.cit.stathis.auth.dto.AuthResponseDTO;
import edu.cit.stathis.auth.dto.CreateUserDTO;
import edu.cit.stathis.auth.dto.LoginDTO;
import edu.cit.stathis.auth.dto.UserResponseDTO;
import edu.cit.stathis.auth.entity.User;
import edu.cit.stathis.auth.service.TokenService;
import edu.cit.stathis.auth.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    User user = userService.createUser(userDTO, userDTO.getUserRole());
    UserResponseDTO response = userService.buildUserResponse(user);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  // DISABLED: Email verification functionality
  @GetMapping("/verify-email")
  public ResponseEntity<String> verifyEmail(@RequestParam("token") String tokenValue) {
    // userService.verifyEmail(tokenValue);
    return new ResponseEntity<>("Email verification is currently disabled.", HttpStatus.OK);
  }

  // DISABLED: Email verification functionality
  @PostMapping("/resend-verification-email")
  public ResponseEntity<?> resendVerification(@RequestParam String email) {
    // try {
    //   String message = userService.resendVerificationEmail(email);
    //   return ResponseEntity.ok(message);
    // } catch (IllegalArgumentException e) {
    //   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    // } catch (IllegalStateException e) {
    //   return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    // }
    return ResponseEntity.ok("Email verification is currently disabled.");
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
    return ResponseEntity.ok(userService.refreshToken(refreshToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestParam String refreshToken) {
    tokenService.revokeToken(refreshToken);
    return ResponseEntity.ok("Logged out");
  }
}
