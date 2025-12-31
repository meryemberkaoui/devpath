package com.devPath.project.service;

import com.devPath.auth.AuthService;
import com.devPath.project.exceptions.ProjectNotFoundException;
import com.devPath.project.mapper.ProjectMapper;
import com.devPath.project.model.Project;
import com.devPath.project.model.dto.ProjectRequest;
import com.devPath.project.model.dto.ProjectResponse;
import com.devPath.project.repository.ProjectRepository;
import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import com.devPath.user.domain.User;
import lombok.*;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final AuthService authService;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        User owner = authService.getCurrentUser();

        Project project = Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .skills(request.getSkills())
                .gitHubURL(request.getGitHubURL())
                .owner(owner)
                .build();

        projectRepository.save(project);

        return projectMapper.toResponse(project);
    }

    @Override
    public ProjectResponse getProjectById(Long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        return projectMapper.toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects(){
        List<Project> projects = projectRepository.findAll();
        return projectMapper.toResponseList(projects);
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request){

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        if (!project.getOwner().getId().equals(authService.getCurrentUserId())) {
            throw new AccessDeniedException("You are not allowed to update this project");
        }

        projectMapper.updateProjectFromRequest(request, project);
        projectRepository.save(project);

        return projectMapper.toResponse(project);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        // Vérification du propriétaire
        if (!project.getOwner().getId().equals(authService.getCurrentUserId())) {
            throw new AccessDeniedException("You are not allowed to delete this project");
        }

        projectRepository.delete(project);
    }

    @Override
    public List<ProjectResponse> getMyProjects(){
        User currentUser = authService.getCurrentUser();
        List<Project> projects = projectRepository.findByOwner(currentUser);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> findByDifficulty(Difficulty difficulty) {
        return projectMapper.toResponseList(
                projectRepository.findByDifficulty(difficulty)
        );
    }

    @Override
    public List<ProjectResponse> searchByTitle(String title) {
        return projectMapper.toResponseList(
                projectRepository.findByTitleContainingIgnoreCase(title)
        );
    }

    @Override
    public List<ProjectResponse> findBySkill(Skill skill) {
        return projectMapper.toResponseList(
                projectRepository.findBySkill(skill)
        );
    }


}
