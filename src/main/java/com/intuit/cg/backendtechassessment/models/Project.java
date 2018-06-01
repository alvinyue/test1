package com.intuit.cg.backendtechassessment.models;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="project", uniqueConstraints = {@UniqueConstraint(columnNames={"id"})})
public class Project {
    public static final long DEFAULT_MIN_BID = Long.MAX_VALUE;
    public static final long DEFAULT_MIN_BID_ID = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id;
    long sellerId;
    String description;
    long maxBudget; //in unit of integer dollar
    long minBid;
    long minBidId;
    LocalDateTime bidDeadline;
    BidStatus bidStatus;

    public enum BidStatus {
        NONE,             //No result yet (this usually the case the deadline is not reached yet.)
        MINIMUM_FOUND,    //found minimum after deadline
        MINIMUM_TOO_HIGH, //found minimum after deadline but it's higher than max bid for project.
        MINIMUM_NOT_FOUND //found no minimum (e.g. no bid) after deadline
    }

    public Project() {

    }

    public Project(long sellerId, String description, long maxBudget, LocalDateTime bidDeadline) {
        this.sellerId = sellerId;
        this.description = description;
        this.maxBudget = maxBudget;
        this.bidDeadline = bidDeadline;

        //these are dynamically calculated
        this.minBid = DEFAULT_MIN_BID;
        this.minBidId = DEFAULT_MIN_BID_ID;
        this.bidStatus = BidStatus.NONE;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSellerId() {
        return sellerId;
    }

    public void setSellerId(long sellerId) {
        this.sellerId = sellerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(long maxBudget) {
        this.maxBudget = maxBudget;
    }

    /** returns the minimum bid up to this moment in time. c getBidStatus() to determin if minBid is final */
    public long getMinBid() { return minBid; }

    public void setMinBid(long minBid) {
        this.minBid = minBid;
    }

    public long getMinBidId() { return minBidId; }

    public void setMinBidId(long minBidId) {
        this.minBidId = minBidId;
    }

    public LocalDateTime getBidDeadline() {
        return bidDeadline;
    }

    public void setBidDeadline(LocalDateTime bidDeadline) {
        this.bidDeadline = bidDeadline;
    }

    public BidStatus getBidStatus() { return bidStatus; }

    public void setBidStatus(BidStatus bidStatus) { this.bidStatus = bidStatus; }

    @Override
    public String toString() {
        return new StringBuilder().append("id=").append(id).append(",description=").append(description)
                .append(",maxBudget=").append(maxBudget).append(",minBid=").append(minBid)
                .append(",bidDeadline=").append(bidDeadline)
                .append(",bidStatus=").append(bidStatus)
                .toString();
    }
}
