package com.mindbloom.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dass21_results")
public class Dass21Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int personId;

    private int depressionScore;
    private int anxietyScore;
    private int stressScore;

    private String depressionSeverity;
    private String anxietySeverity;
    private String stressSeverity;

    private LocalDateTime completedAt;

    // ===== Constructors =====
    public Dass21Result() {
    }

    // ===== Getters & Setters =====
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public int getDepressionScore() {
        return depressionScore;
    }

    public void setDepressionScore(int depressionScore) {
        this.depressionScore = depressionScore;
    }

    public int getAnxietyScore() {
        return anxietyScore;
    }

    public void setAnxietyScore(int anxietyScore) {
        this.anxietyScore = anxietyScore;
    }

    public int getStressScore() {
        return stressScore;
    }

    public void setStressScore(int stressScore) {
        this.stressScore = stressScore;
    }

    public String getDepressionSeverity() {
        return depressionSeverity;
    }

    public void setDepressionSeverity(String depressionSeverity) {
        this.depressionSeverity = depressionSeverity;
    }

    public String getAnxietySeverity() {
        return anxietySeverity;
    }

    public void setAnxietySeverity(String anxietySeverity) {
        this.anxietySeverity = anxietySeverity;
    }

    public String getStressSeverity() {
        return stressSeverity;
    }

    public void setStressSeverity(String stressSeverity) {
        this.stressSeverity = stressSeverity;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
