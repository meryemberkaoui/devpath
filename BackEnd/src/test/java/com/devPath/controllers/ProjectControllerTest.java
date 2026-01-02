package com.devPath.controllers;

import com.devPath.project.controller.ProjectController;
import com.devPath.project.exceptions.ProjectNotFoundException;
import com.devPath.project.model.dto.ProjectRequest;
import com.devPath.project.model.dto.ProjectResponse;
import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import com.devPath.project.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
/**
 * TODO : ignore Spring Security pour les tests
 * ( à voir si prise en cpt de securité)*/
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    private ProjectRequest validRequest;
    private ProjectResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ProjectRequest(
                "Test project",
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                "https://github.com/user/repo"
        );

        validResponse = ProjectResponse.builder()
                .id(1L)
                .title(validRequest.getTitle())
                .gitHubURL(validRequest.getGitHubURL())
                .build();
    }

    // ===== POST =====
    @Test
    void createProject_shouldReturn201() throws Exception {
        when(projectService.createProject(any())).thenReturn(validResponse);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test project"));
    }

    @Test
    void createProject_withMissingTitle_shouldReturn400() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "", "Desc", Difficulty.BEGINNER, Set.of(Skill.JAVA), null
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== GET =====
    @Test
    void getProject_shouldReturnProject() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(validResponse);

        mockMvc.perform(get("/api/projects/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test project"));
    }

    @Test
    void getProject_notFound_shouldReturn404() throws Exception {
        when(projectService.getProjectById(999L))
                .thenThrow(new ProjectNotFoundException(999L));

        mockMvc.perform(get("/api/projects/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProject_otherError_shouldReturn500() throws Exception {
        when(projectService.getProjectById(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/projects/{id}", 1L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllProjects_shouldReturnList() throws Exception {
        when(projectService.getAllProjects())
                .thenReturn(List.of(validResponse, validResponse));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ===== PUT =====
    @Test
    void updateProject_shouldReturnUpdated() throws Exception {
        ProjectResponse updatedResponse = ProjectResponse.builder()
                .id(1L)
                .title("Updated")
                .build();

        when(projectService.updateProject(eq(1L), any())).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/projects/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void updateProject_notFound_shouldReturn404() throws Exception {
        when(projectService.updateProject(eq(999L), any()))
                .thenThrow(new ProjectNotFoundException(999L));

        mockMvc.perform(put("/api/projects/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProject_otherError_shouldReturn500() throws Exception {
        when(projectService.updateProject(eq(1L), any()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/projects/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    // ===== DELETE =====
    @Test
    void deleteProject_shouldReturn204() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProject_notFound_shouldReturn404() throws Exception {
        doThrow(new ProjectNotFoundException(999L))
                .when(projectService).deleteProject(999L);

        mockMvc.perform(delete("/api/projects/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProject_otherError_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/{id}", 1L))
                .andExpect(status().isInternalServerError());
    }

    // ===== SEARCH / FILTER =====
    @Test
    void searchByTitle_shouldReturnList() throws Exception {
        when(projectService.searchByTitle("test"))
                .thenReturn(List.of(validResponse));

        mockMvc.perform(get("/api/projects/search").param("title", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByDifficulty_shouldReturnList() throws Exception {
        when(projectService.findByDifficulty(Difficulty.BEGINNER))
                .thenReturn(List.of(validResponse));

        mockMvc.perform(get("/api/projects/difficulty/BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBySkill_shouldReturnList() throws Exception {
        when(projectService.findBySkill(Skill.JAVA))
                .thenReturn(List.of(validResponse));

        mockMvc.perform(get("/api/projects/skill/JAVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ===== POST Validation Tests =====

    @Test
    void createProject_withEmptySkills_shouldReturn400() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "Test project",
                "Description",
                Difficulty.BEGINNER,
                Set.of(),  // skills vide -> doit échouer
                "https://github.com/user/repo"
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProject_withInvalidGitHubURL_shouldReturn400() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "Test project",
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                "invalid-url"  // URL invalide -> doit échouer
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}
