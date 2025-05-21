package edu.cit.stathis.classroom.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomResponseDTO {
    private String physicalId;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
    private boolean isActive;
    private String teacherName;
    private int studentCount;
}
