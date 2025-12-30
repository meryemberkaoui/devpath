package com.devPath.project.model.dto;

import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private Long id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Set<Skill> skills;
    private String githubUrl;
    private LocalDateTime createdAt;

    // Info about the owner who react project
    // (without calling User object)
    private Long ownerId;
    private String ownerUsername;
    private String ownerEmail;
}
