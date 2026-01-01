package com.devPath.services;

import com.devPath.auth.AuthService;
import com.devPath.project.exceptions.ProjectNotFoundException;
import com.devPath.project.mapper.ProjectMapper;
import com.devPath.project.model.Project;
import com.devPath.project.model.dto.ProjectRequest;
import com.devPath.project.model.dto.ProjectResponse;
import com.devPath.project.repository.ProjectRepository;
import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import com.devPath.project.service.ProjectServiceImpl;
import com.devPath.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final User TEST_USER = createTestUser();

    private User createTestUser() {
        return User.builder()
                .id(TEST_USER_ID)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    // ========= CREATE PROJECT =========
    @Test
    void createProject_shouldCreateAndReturnProject() {
        // Given
        ProjectRequest request = new ProjectRequest(
                "Test Project",
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                "https://github.com/test/project"
        );

        ProjectResponse expectedResponse = ProjectResponse.builder()
                .id(1L)
                .title("Test Project")
                .owner_id(TEST_USER_ID)
                .build();

        when(authService.getCurrentUser()).thenReturn(TEST_USER);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(1L); // Simule l'ID généré par la base
            return project;
        });
        when(projectMapper.toResponse(any(Project.class))).thenReturn(expectedResponse);

        // When
        ProjectResponse result = projectService.createProject(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Project", result.getTitle());

        verify(authService).getCurrentUser();
        verify(projectRepository).save(any(Project.class));
        verify(projectMapper).toResponse(any(Project.class));
    }

    // ========= GET PROJECT BY ID =========
    @Test
    void getProjectById_shouldReturnProject() {
        // Given
        Long projectId = 1L;
        Project project = Project.builder().id(projectId).title("My Project").build();
        ProjectResponse expectedResponse = ProjectResponse.builder().id(projectId).title("My Project").build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(any(Project.class))).thenReturn(expectedResponse);

        // When
        ProjectResponse result = projectService.getProjectById(projectId);

        // Then
        assertNotNull(result);
        assertEquals(projectId, result.getId());
        assertEquals("My Project", result.getTitle());

        verify(projectRepository).findById(projectId);
        verify(projectMapper).toResponse(any(Project.class));
    }

    @Test
    void getProjectById_notFound_shouldThrowException() {
        // Given
        Long projectId = 999L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProjectNotFoundException.class, () ->
                projectService.getProjectById(projectId)
        );

        verify(projectRepository).findById(projectId);
        verify(projectMapper, never()).toResponse(any());
    }

    // ========= GET ALL PROJECTS =========
    @Test
    void getAllProjects_shouldReturnList() {
        // Given
        List<Project> projects = List.of(
                Project.builder().id(1L).title("Project 1").build(),
                Project.builder().id(2L).title("Project 2").build()
        );

        List<ProjectResponse> expectedResponses = List.of(
                ProjectResponse.builder().id(1L).title("Project 1").build(),
                ProjectResponse.builder().id(2L).title("Project 2").build()
        );

        when(projectRepository.findAll()).thenReturn(projects);
        when(projectMapper.toResponseList(projects)).thenReturn(expectedResponses);

        // When
        List<ProjectResponse> result = projectService.getAllProjects();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(projectRepository).findAll();
        verify(projectMapper).toResponseList(projects);
    }

    // ========= UPDATE PROJECT =========
    @Test
    void updateProject_shouldUpdateAndReturnProject() {
        // Given
        Long projectId = 1L;
        ProjectRequest request = new ProjectRequest(
                "Updated",
                "Updated desc",
                Difficulty.INTERMEDIATE,
                Set.of(Skill.ANGULAR),
                null
        );

        Project existingProject = Project.builder()
                .id(projectId)
                .title("Old Title")
                .owner(TEST_USER)
                .build();

        ProjectResponse expectedResponse = ProjectResponse.builder()
                .id(projectId)
                .title("Updated")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(expectedResponse);

        // When
        ProjectResponse result = projectService.updateProject(projectId, request);

        // Then
        assertNotNull(result);
        assertEquals("Updated", result.getTitle());

        verify(projectRepository).findById(projectId);
        verify(authService).getCurrentUserId();
        verify(projectMapper).updateProjectFromRequest(request, existingProject);
        verify(projectRepository).save(existingProject);
        verify(projectMapper).toResponse(any(Project.class));
    }

    @Test
    void updateProject_notFound_shouldThrowException() {
        // Given
        Long projectId = 999L;
        ProjectRequest request = new ProjectRequest(
                "Updated",
                "Description",
                Difficulty.BEGINNER,
                Set.of(Skill.JAVA),
                null
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProjectNotFoundException.class, () ->
                projectService.updateProject(projectId, request)
        );

        verify(projectRepository).findById(projectId);
        verify(authService, never()).getCurrentUserId();
    }

    @Test
    void updateProject_notOwner_shouldThrowAccessDenied() {
        // Given
        Long projectId = 1L;
        ProjectRequest request = new ProjectRequest("Updated", "Desc", Difficulty.BEGINNER, Set.of(Skill.JAVA), null);

        User otherUser = User.builder().id(UUID.randomUUID()).build();
        Project existingProject = Project.builder().id(projectId).owner(otherUser).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                projectService.updateProject(projectId, request)
        );

        verify(projectRepository).findById(projectId);
        verify(authService).getCurrentUserId();
        verify(projectMapper, never()).updateProjectFromRequest(any(), any());
    }

    // ========= DELETE PROJECT =========
    @Test
    void deleteProject_shouldDeleteProject() {
        // Given
        Long projectId = 1L;
        Project project = Project.builder().id(projectId).owner(TEST_USER).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // When
        projectService.deleteProject(projectId);

        // Then
        verify(projectRepository).findById(projectId);
        verify(authService).getCurrentUserId();
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProject_notFound_shouldThrowException() {
        // Given
        Long projectId = 999L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProjectNotFoundException.class, () ->
                projectService.deleteProject(projectId)
        );

        verify(projectRepository).findById(projectId);
        verify(authService, never()).getCurrentUserId();
    }

    @Test
    void deleteProject_notOwner_shouldThrowAccessDenied() {
        // Given
        Long projectId = 1L;
        User otherUser = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(projectId).owner(otherUser).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                projectService.deleteProject(projectId)
        );

        verify(projectRepository).findById(projectId);
        verify(authService).getCurrentUserId();
        verify(projectRepository, never()).delete(any());
    }

    // ========= GET MY PROJECTS =========
    @Test
    void getMyProjects_shouldReturnUserProjects() {
        // Given
        List<Project> userProjects = List.of(
                Project.builder().id(1L).title("My Project 1").build(),
                Project.builder().id(2L).title("My Project 2").build()
        );

        List<ProjectResponse> expectedResponses = List.of(
                ProjectResponse.builder().id(1L).title("My Project 1").build(),
                ProjectResponse.builder().id(2L).title("My Project 2").build()
        );

        when(authService.getCurrentUser()).thenReturn(TEST_USER);
        when(projectRepository.findByOwner(TEST_USER)).thenReturn(userProjects);
        when(projectMapper.toResponseList(userProjects)).thenReturn(expectedResponses);

        // When
        List<ProjectResponse> result = projectService.getMyProjects();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(authService).getCurrentUser();
        verify(projectRepository).findByOwner(TEST_USER);
        verify(projectMapper).toResponseList(userProjects);
    }

    // ========= FIND BY DIFFICULTY =========
    @Test
    void findByDifficulty_shouldReturnFilteredProjects() {
        // Given
        Difficulty difficulty = Difficulty.BEGINNER;
        List<Project> projects = List.of(Project.builder().id(1L).difficulty(difficulty).build());
        List<ProjectResponse> expectedResponses = List.of(ProjectResponse.builder().id(1L).difficulty(difficulty).build());

        when(projectRepository.findByDifficulty(difficulty)).thenReturn(projects);
        when(projectMapper.toResponseList(projects)).thenReturn(expectedResponses);

        // When
        List<ProjectResponse> result = projectService.findByDifficulty(difficulty);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(difficulty, result.get(0).getDifficulty());

        verify(projectRepository).findByDifficulty(difficulty);
        verify(projectMapper).toResponseList(projects);
    }

    // ========= SEARCH BY TITLE =========
    @Test
    void searchByTitle_shouldReturnMatchingProjects() {
        // Given
        String searchTerm = "test";
        List<Project> projects = List.of(Project.builder().id(1L).title("Test Project").build());
        List<ProjectResponse> expectedResponses = List.of(ProjectResponse.builder().id(1L).title("Test Project").build());

        when(projectRepository.findByTitleContainingIgnoreCase(searchTerm)).thenReturn(projects);
        when(projectMapper.toResponseList(projects)).thenReturn(expectedResponses);

        // When
        List<ProjectResponse> result = projectService.searchByTitle(searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getTitle());

        verify(projectRepository).findByTitleContainingIgnoreCase(searchTerm);
        verify(projectMapper).toResponseList(projects);
    }

    // ========= FIND BY SKILL =========
    @Test
    void findBySkill_shouldReturnProjectsWithSkill() {
        // Given
        Skill skill = Skill.JAVA;
        List<Project> projects = List.of(Project.builder().id(1L).skills(Set.of(skill)).build());
        List<ProjectResponse> expectedResponses = List.of(ProjectResponse.builder().id(1L).skills(Set.of(skill)).build());

        when(projectRepository.findBySkill(skill)).thenReturn(projects);
        when(projectMapper.toResponseList(projects)).thenReturn(expectedResponses);

        // When
        List<ProjectResponse> result = projectService.findBySkill(skill);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getSkills().contains(skill));

        verify(projectRepository).findBySkill(skill);
        verify(projectMapper).toResponseList(projects);
    }
}