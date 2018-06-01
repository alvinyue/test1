package com.intuit.cg.backendtechassessment.repository;

import com.intuit.cg.backendtechassessment.models.Seller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class SellerRepositoryImpl implements SellerRepository{
    private static Logger log = LoggerFactory.getLogger(SellerRepositoryImpl.class);

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
    public List<Seller> findAll() {
        Query query = this.getEntityManager().createQuery("SELECT p FROM Seller p");
        return query.getResultList();
    }

    @Override
    public Seller find(long id) {
        return this.getEntityManager().find(Seller.class, id);
    }

    @Override
    public void save(Seller seller) {
        if(seller != null && seller.getId() == 0) {
            em.persist(seller); //new
        } else {
            em.merge(seller); //update
        }
    }

    @Override
    public void persist(Seller seller) {
        if(seller != null) {
            em.persist(seller);
        }
    }

    @Override
    public void merge(Seller seller) {
        if (seller != null) {
            em.merge(seller);
        }
    }

    @Override
    public void remove(long id) {
        Seller seller = find(id);
        if (seller != null) {
            em.remove(seller);
        }
    }
}
