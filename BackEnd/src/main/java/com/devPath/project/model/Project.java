package com.devPath.project.model;

import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @CollectionTable(name = "project_skills",
            joinColumns = @JoinColumn(name = "project_id"))
    @Enumerated(EnumType.STRING)
    private Set<Skill> skills;

    private String gitHubURL;

    @CreationTimestamp
    private LocalDateTime createdAt;

    //TODO : replace type User from string to entity User
    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "owner_id", nullable = false)
    private String owner_id;





}
