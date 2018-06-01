package com.intuit.cg.backendtechassessment.models;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name="bid", uniqueConstraints = {@UniqueConstraint(columnNames={"id"})})
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id;
    long amount;
    LocalDateTime bidDate;
    long projectId;
    long buyerId;

    boolean processed;
    boolean autoBid;
    long minAmount;   //minimum amount for auto bid mode
    long winningBid;  //remember the winning bid for auto bid mode

    public Bid() {

    }

    /** for fixed bids */
    public Bid(long amount, LocalDateTime bidDate, long projectId, long buyerId) {
        this.amount = amount;
        this.bidDate = bidDate;
        this.projectId = projectId;
        this.buyerId = buyerId;
        this.autoBid = false;
        this.minAmount = 0;
        this.winningBid = 0;
    }

    /**
     * for auto bids,
     * @param minAmount defines the lowest bid auto bid can reach
     */
    public Bid(long amount, LocalDateTime bidDate, long projectId, long buyerId, boolean autoBid, long minAmount) {
        this.amount = amount;
        this.bidDate = bidDate;
        this.projectId = projectId;
        this.buyerId = buyerId;
        this.autoBid = autoBid;
        this.minAmount = minAmount;
        this.winningBid = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /** returns fixed bid's amount in unit of dollar */
    public long getAmount() {
        return amount;
    }

    /** set fixed bid's amount in unit of dollar */
    public void setAmount(long amount) {
        this.amount = amount;
    }

    /** returns auto bid's minimum amount in unit of dollar */
    public long getMinAmount() {
        return minAmount;
    }

    /** set auto bid's minimum amount in unit of dollar */
    public void setMinAmount(long minAmount) {
        this.minAmount = minAmount;
    }

    public LocalDateTime getBidDate() {
        return bidDate;
    }

    public void setBidDate(LocalDateTime bidDate) {
        this.bidDate = bidDate;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(long buyerId) {
        this.buyerId = buyerId;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isAutoBid() {
        return autoBid;
    }

    public void setAutoBid(boolean autoBid) {
        this.autoBid = autoBid;
    }

    public long getWinningBid() {
        return winningBid;
    }

    public void setWinningBid(long winningBid) {
        this.winningBid = winningBid;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("id=");
        sb.append(id).append(",amount=").append(amount).append(",bidDate=").append(bidDate)
                .append(",projectId=").append(projectId).append(",buyerId=").append(buyerId)
                .append(",processed=").append(processed).append(",autoBid=").append(autoBid)
                .append(",minAmount=").append(minAmount).append(",winningBid=").append(winningBid);
        return sb.toString();
    }
}
