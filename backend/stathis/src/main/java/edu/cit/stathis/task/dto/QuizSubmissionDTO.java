package edu.cit.stathis.task.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionDTO {
    // Index-aligned list of chosen option indices per question (0-based)
    private List<Integer> answers;
}


