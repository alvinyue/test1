package com.intuit.cg.backendtechassessment.models;

import javax.persistence.*;

@Entity
@Table(name="seller", uniqueConstraints = {@UniqueConstraint(columnNames={"id"})})
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id;
    String name;

    public Seller() {}

    public Seller(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("id=").append(id).append(",name=").append(name).toString();
    }
}
