package com.devPath.project.repository;

import com.devPath.project.model.Project;
import com.devPath.project.resources.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwnerId(Long ownerId);

    List<Project> findByDifficulty(Difficulty difficulty);

    List<Project> findByTitleContainingIgnoreCase(String title);

    //Vérifier si un projet existe pour un owner avec un titre donné
    boolean existsByTitleAndOwnerId(String title, Long ownerId);

    Optional<Project> findByTitleAndOwnerId(String title, Long ownerId);

    //Recherche personnalisée : projets par compétence (skill)
    @Query("SELECT DISTINCT p FROM Project p JOIN p.skills s WHERE LOWER(s) = LOWER(:skill)")
    List<Project> findBySkill(@Param("skill") String skill);

    //Projets récents (les 10 derniers)
    @Query("SELECT p FROM Project p ORDER BY p.createdAt DESC LIMIT 10")
    List<Project> findRecentProjects();


}
