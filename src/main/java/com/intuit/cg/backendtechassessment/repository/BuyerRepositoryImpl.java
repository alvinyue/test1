package com.intuit.cg.backendtechassessment.repository;

import com.intuit.cg.backendtechassessment.models.Bid;
import com.intuit.cg.backendtechassessment.models.Buyer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class BuyerRepositoryImpl implements BuyerRepository{
    private static Logger log = LoggerFactory.getLogger(BuyerRepositoryImpl.class);

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
    public List<Buyer> findAll() {
        Query query = this.getEntityManager().createQuery("SELECT p FROM Buyer p");
        return query.getResultList();
    }

    @Override
    public Buyer find(long id) {
        return this.getEntityManager().find(Buyer.class, id);
    }

    @Override
    public void save(Buyer buyer) {
        if(buyer != null && buyer.getId() == 0) {
            em.persist(buyer); //new
        } else {
            em.merge(buyer); //update
        }
    }

    @Override
    public void persist(Buyer buyer) {
        if(buyer != null) {
            em.persist(buyer);
        }
    }

    @Override
    public void merge(Buyer buyer) {
        if (buyer != null) {
            em.merge(buyer);
        }
    }

    @Override
    public void remove(long id) {
        Buyer buyer = find(id);
        if (buyer != null) {
            em.remove(buyer);
        }
    }
}
