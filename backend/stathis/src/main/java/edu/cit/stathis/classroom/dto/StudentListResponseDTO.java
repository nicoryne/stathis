package edu.cit.stathis.classroom.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentListResponseDTO {
    private String physicalId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;
    private String joinedAt;
    private boolean isVerified;
}
