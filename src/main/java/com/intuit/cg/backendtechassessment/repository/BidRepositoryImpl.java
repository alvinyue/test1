package com.intuit.cg.backendtechassessment.repository;

import com.intuit.cg.backendtechassessment.models.Bid;
import com.intuit.cg.backendtechassessment.models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class BidRepositoryImpl implements BidRepository{
    private static Logger log = LoggerFactory.getLogger(BidRepositoryImpl.class);

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
    public List<Bid> findAll() {
        Query query = this.getEntityManager().createQuery("SELECT p FROM Bid p");
        return query.getResultList();
    }

    @Override
    public List<Bid> findAllByProject(long projectId) {
        Query query = this.getEntityManager().createQuery("SELECT p FROM Bid p WHERE p.projectId=:projectId").
                setParameter("projectId", projectId);
        return query.getResultList();
    }

    @Override
    public List<Bid> findProcessedByProject(long projectId, boolean processed) {
        Query query = this.getEntityManager().createQuery("SELECT p FROM Bid p WHERE p.projectId=:projectId and p.processed=:processed").
                setParameter("projectId", projectId).
                setParameter("processed", processed);
        return query.getResultList();
    }

    @Override
    public List<Bid> findNonAutoProcessedByProject(long projectId, boolean processed) {
        Query query = this.getEntityManager().createQuery("SELECT p FROM Bid p WHERE p.projectId=:projectId and p.processed=:processed and p.autoBid=false").
                setParameter("projectId", projectId).
                setParameter("processed", processed);
        return query.getResultList();
    }

    @Override
    public List<Bid> findAutoByProject(long projectId){
        Query query = this.getEntityManager().createQuery("SELECT p FROM Bid p WHERE p.projectId=:projectId and p.autoBid=true order by p.minAmount").
                setParameter("projectId", projectId);
        return query.getResultList();
    }

    @Override
    public Bid find(long id) {
        return this.getEntityManager().find(Bid.class, id);
    }

    @Override
    public void save(Bid bid) {
        if(bid != null && bid.getId() == 0) {
            em.persist(bid); //new
        } else {
            em.merge(bid); //update
        }
    }

    @Override
    public void persist(Bid bid) {
        if(bid != null) {
            em.persist(bid);
        }
    }

    @Override
    public void merge(Bid bid) {
        if (bid != null) {
            em.merge(bid);
        }
    }

    @Override
    public void remove(long id) {
        Bid bid = find(id);
        if (bid != null) {
            em.remove(bid);
        }
    }
}
