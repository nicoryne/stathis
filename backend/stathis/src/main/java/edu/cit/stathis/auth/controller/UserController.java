package edu.cit.stathis.auth.controller;

import edu.cit.stathis.auth.dto.UpdateStudentProfileDTO;
import edu.cit.stathis.auth.dto.UpdateTeacherProfileDTO;
import edu.cit.stathis.auth.dto.UpdateUserProfileDTO;
import edu.cit.stathis.auth.dto.UserResponseDTO;
import edu.cit.stathis.auth.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints related to user accounts")
public class UserController {

  @Autowired private UserService userService;

  @PutMapping("/profile")
  public ResponseEntity<UserResponseDTO> updateUserProfile(@RequestBody UpdateUserProfileDTO profileDTO) {
    UserResponseDTO response = userService.updateUserProfile(profileDTO);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PutMapping("/profile/student")
  public ResponseEntity<UserResponseDTO> updateStudentProfile(@RequestBody UpdateStudentProfileDTO studentDTO) {
    UserResponseDTO response = userService.updateStudentProfile(studentDTO);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PutMapping("/profile/teacher")
  public ResponseEntity<UserResponseDTO> updateTeacherProfile(@RequestBody UpdateTeacherProfileDTO teacherDTO) {
    UserResponseDTO response = userService.updateTeacherProfile(teacherDTO);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/profile/student")
  public ResponseEntity<UserResponseDTO> getStudentProfile() {
    UserResponseDTO response = userService.getStudentUserProfile();
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/profile/teacher")
  public ResponseEntity<UserResponseDTO> getTeacherProfile() {
    UserResponseDTO response = userService.getTeacherUserProfile();
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
