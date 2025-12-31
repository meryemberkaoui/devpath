package com.devPath.project.repository;

import com.devPath.project.model.Project;
import com.devPath.project.resources.Difficulty;
import com.devPath.project.resources.Skill;
import com.devPath.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(User owner);

    List<Project> findByDifficulty(Difficulty difficulty);

    List<Project> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT DISTINCT p FROM Project p JOIN p.skills s WHERE s = :skill")
    List<Project> findBySkill(@Param("skill") Skill skill);

}
