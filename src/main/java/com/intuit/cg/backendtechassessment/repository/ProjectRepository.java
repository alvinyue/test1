package com.intuit.cg.backendtechassessment.repository;

import com.intuit.cg.backendtechassessment.models.Project;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository {
    public List<Project> findAll();
    public Project find(long id);
    public void save(Project project);
    public void persist(Project project);
    public void merge(Project project);

    public void remove(long id);
}
