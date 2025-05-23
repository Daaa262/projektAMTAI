package com.projektamtai.projekt.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "extinguisher")
public class ExtinguisherModel {
    @Id
    @Column(name = "ID", nullable = false, length = 10)
    private String id;

    @Column(name = "Expire", nullable = false)
    private Instant expire;

    @ColumnDefault("0")
    @Column(name = "Used", nullable = false)
    private Boolean used = false;

    @Column(name = "Location", length = 200)
    private String location;

    @Column(name = "Notes", length = 500)
    private String notes;

    public ExtinguisherModel() {
    }

    public ExtinguisherModel(String id, Instant expire, Boolean used, String location, String notes) {
        this.id = id;
        this.expire = expire;
        this.used = used;
        this.location = location;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getExpire() {
        return expire;
    }

    public void setExpire(Instant expire) {
        this.expire = expire;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}