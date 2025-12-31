package com.devPath.project.model.dto;

import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotEmpty(message = "At least one skill is required")
    private Set<Skill> skills;

    @Pattern(
            regexp = "^(https?://)?(www\\\\.)?github\\\\.com/.+",
            message = "Invalid GitHub URL"
    )
    private String gitHubURL;

}
