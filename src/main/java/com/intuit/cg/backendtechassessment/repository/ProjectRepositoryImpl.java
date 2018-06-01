package com.intuit.cg.backendtechassessment.repository;

import com.intuit.cg.backendtechassessment.models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ProjectRepositoryImpl implements ProjectRepository{
    private static Logger log = LoggerFactory.getLogger(ProjectRepositoryImpl.class);

    private EntityManager em;

    private EntityManager getEntityManager() {
        return em;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.debug("setEntityManager() ");
        this.em = em;
    }

    @Override
    public List<Project> findAll() {
        Query query = this.getEntityManager().createQuery("SELECT p FROM Project p");
        return query.getResultList();
    }

    @Override
    public Project find(long id) {
        return this.getEntityManager().find(Project.class, id);
    }

    @Override
    public void save(Project project) {
        if(project != null && project.getId() == 0) {
            em.persist(project); //new
        } else {
            em.merge(project); //update
        }
    }

    @Override
    public void persist(Project project) {
        if(project != null) {
            em.persist(project);
        }
    }

    @Override
    public void merge(Project project) {
        if (project != null) {
            em.merge(project);
        }
    }

    @Override
    public void remove(long id) {
        Project project = find(id);
        if (project != null) {
            em.remove(project);
        }
    }
}
