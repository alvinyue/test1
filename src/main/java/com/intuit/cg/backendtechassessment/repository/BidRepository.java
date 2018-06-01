package com.intuit.cg.backendtechassessment.repository;

import com.intuit.cg.backendtechassessment.models.Bid;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository {
    public List<Bid> findAll();
    public List<Bid> findAllByProject(long projectId);
    /** filter out bids that are processed or not processed (includes auto and non auto bids) */
    public List<Bid> findProcessedByProject(long projectId, boolean processed);
    /** filter out auto bids. */
    public List<Bid> findNonAutoProcessedByProject(long projectId, boolean processed);
    /** filter out non auto bids and sort auto bids (minimum bid) in ascending order. */
    public List<Bid> findAutoByProject(long projectId);

    public Bid find(long id);
    public void save(Bid bid);
    public void persist(Bid bid);
    public void merge(Bid bid);

    public void remove(long id);
}
