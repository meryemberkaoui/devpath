package com.devPath.project.mapper;

import com.devPath.project.model.Project;
import com.devPath.project.model.dto.ProjectRequest;
import com.devPath.project.model.dto.ProjectResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    //Convert ProjectRequest to Project (Entity)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "owner", ignore = true)
    Project toEntity(ProjectRequest request);

    //Convert Project (Entity) to ProjectResponse
    @Mapping(target = "owner_id", source = "owner.id")
    @Mapping(target = "owner_username", source = "owner.username")
    @Mapping(target = "owner_email", source = "owner.email")
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void updateProjectFromRequest(ProjectRequest request, @MappingTarget Project project);

}
