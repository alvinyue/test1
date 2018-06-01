package com.intuit.cg.backendtechassessment.repository;

import com.intuit.cg.backendtechassessment.models.Seller;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerRepository {
    public List<Seller> findAll();
    public Seller find(long id);
    public void save(Seller project);
    public void persist(Seller project);
    public void merge(Seller project);

    public void remove(long id);
}
