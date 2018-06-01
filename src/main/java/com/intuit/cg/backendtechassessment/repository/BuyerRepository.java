package com.intuit.cg.backendtechassessment.repository;

import com.intuit.cg.backendtechassessment.models.Bid;
import com.intuit.cg.backendtechassessment.models.Buyer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerRepository {
    public List<Buyer> findAll();
    public Buyer find(long id);
    public void save(Buyer project);
    public void persist(Buyer project);
    public void merge(Buyer project);

    public void remove(long id);
}
