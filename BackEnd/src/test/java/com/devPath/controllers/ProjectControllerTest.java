package com.devPath.controllers;

import com.devPath.project.model.dto.*;
import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import com.devPath.project.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(com.fasterxml.jackson.databind.ObjectMapper.class)
@SpringBootTest
class ProjectControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // ========= POST =========
    @Test
    void createProject_shouldReturn201() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "Test project",
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                null
        );

        ProjectResponse response = new ProjectResponse();
        response.setId(1L);
        response.setTitle("Test project");

        when(projectService.createProject(any())).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test project"));
    }

    // ========= GET by ID =========
    @Test
    void getProject_shouldReturnProject() throws Exception {
        ProjectResponse response = new ProjectResponse();
        response.setId(1L);
        response.setTitle("My project");

        when(projectService.getProjectById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/projects/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("My project"));
    }

    // ========= GET ALL =========
    @Test
    void getAllProjects_shouldReturnList() throws Exception {
        when(projectService.getAllProjects())
                .thenReturn(List.of(new ProjectResponse(), new ProjectResponse()));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ========= PUT =========
    @Test
    void updateProject_shouldReturnUpdated() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "Updated",
                "Updated desc",
                Difficulty.INTERMEDIATE,
                Set.of(Skill.ANGULAR),
                null
        );

        ProjectResponse response = new ProjectResponse();
        response.setTitle("Updated");

        when(projectService.updateProject(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/projects/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    // ========= DELETE =========
    @Test
    void deleteProject_shouldReturn204() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    // ========= SEARCH =========
    @Test
    void searchByTitle_shouldReturnList() throws Exception {
        when(projectService.searchByTitle("test"))
                .thenReturn(List.of(new ProjectResponse()));

        mockMvc.perform(get("/api/projects/search")
                        .param("title", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ========= DIFFICULTY =========
    @Test
    void getByDifficulty_shouldReturnList() throws Exception {
        when(projectService.findByDifficulty(Difficulty.BEGINNER))
                .thenReturn(List.of(new ProjectResponse()));

        mockMvc.perform(get("/api/projects/difficulty/BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ========= SKILL =========
    @Test
    void getBySkill_shouldReturnList() throws Exception {
        when(projectService.findBySkill(Skill.JAVA))
                .thenReturn(List.of(new ProjectResponse()));

        mockMvc.perform(get("/api/projects/skill/JAVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // Test 9: Validation - titre manquant
    @Test
    void createProject_withMissingTitle_shouldReturn400() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "",  // Titre vide -> devrait échouer
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                null
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());  // HTTP 400
    }

    // Test 10: Validation - skills vide
    @Test
    void createProject_withEmptySkills_shouldReturn400() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "Test",
                "Description",
                Difficulty.BEGINNER,
                Set.of(),  // Set vide -> devrait échouer
                null
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test 11: Validation - URL GitHub invalide
    @Test
    void createProject_withInvalidGitHubURL_shouldReturn400() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "Test",
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                "invalid-url"  // Ne match pas le pattern -> devrait échouer
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test 12: Validation - URL GitHub valide
    @Test
    void createProject_withValidGitHubURL_shouldReturn201() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "Test",
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                "https://github.com/username/repo"  // URL valide
        );

        ProjectResponse response = ProjectResponse.builder()
                .id(1L)
                .title("Test")
                .gitHubURL("https://github.com/username/repo")
                .build();

        when(projectService.createProject(any())).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gitHubURL").value("https://github.com/username/repo"));
    }

    // Test 13: Projet non trouvé (404)
    @Test
    void getProject_notFound_shouldReturn404() throws Exception {
        when(projectService.getProjectById(999L))
                .thenThrow(new RuntimeException("Project not found"));  // Ou une exception spécifique

        mockMvc.perform(get("/api/projects/{id}", 999L))
                .andExpect(status().isNotFound());  // HTTP 404
    }

    // Test 14: Mise à jour d'un projet inexistant
    @Test
    void updateProject_notFound_shouldReturn404() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "Updated",
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                null
        );

        when(projectService.updateProject(eq(999L), any()))
                .thenThrow(new RuntimeException("Project not found"));

        mockMvc.perform(put("/api/projects/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // Test DELETE - projet non trouvé
    @Test
    void deleteProject_notFound_shouldReturn404() throws Exception {
        doThrow(new RuntimeException("Project not found"))
                .when(projectService).deleteProject(999L);

        mockMvc.perform(delete("/api/projects/{id}", 999L))
                .andExpect(status().isNotFound());  // HTTP 404
    }

    // Test DELETE - autre erreur
    @Test
    void deleteProject_otherError_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/{id}", 1L))
                .andExpect(status().isInternalServerError());  // HTTP 500
    }

    // Test GET - autre erreur (500)
    @Test
    void getProject_otherError_shouldReturn500() throws Exception {
        when(projectService.getProjectById(1L))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/projects/{id}", 1L))
                .andExpect(status().isInternalServerError());
    }

    // Test PUT - autre erreur (500)
    @Test
    void updateProject_otherError_shouldReturn500() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "Updated",
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                null
        );

        when(projectService.updateProject(eq(1L), any()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/projects/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // Test POST - validation échouée (400)
    @Test
    void createProject_withEmptyTitle_shouldReturn400() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "",  // Titre vide -> validation échoue
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                null
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}