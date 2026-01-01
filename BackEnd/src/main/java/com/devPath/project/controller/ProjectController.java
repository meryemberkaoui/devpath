package com.devPath.project.controller;

import com.devPath.project.model.dto.*;
import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import com.devPath.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        try {
            ProjectResponse response = projectService.getProjectById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        try {
            ProjectResponse updatedProject = projectService.updateProject(id, request);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/myprojects")
    public ResponseEntity<List<ProjectResponse>> getMyProjects() {
        return ResponseEntity.ok(projectService.getMyProjects());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponse>> searchByTitle(
            @RequestParam String title) {
        return ResponseEntity.ok(projectService.searchByTitle(title));
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<ProjectResponse>> getByDifficulty(
            @PathVariable Difficulty difficulty) {
        return ResponseEntity.ok(projectService.findByDifficulty(difficulty));
    }

    @GetMapping("/skill/{skill}")
    public ResponseEntity<List<ProjectResponse>> getBySkill(
            @PathVariable Skill skill) {
        return ResponseEntity.ok(projectService.findBySkill(skill));
    }






}
