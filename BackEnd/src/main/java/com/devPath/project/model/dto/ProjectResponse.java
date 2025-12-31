package com.devPath.project.model.dto;

import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

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
    private String gitHubURL;
    private LocalDateTime createdAt;

    // Info about the owner who react project
    // (without calling User object)
    private UUID owner_id;
    private String owner_username;
    private String owner_email;
}
