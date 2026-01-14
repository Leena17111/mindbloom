package com.mindbloom.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dass21_questions")
public class Dass21Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DassCategory category;

    // ===== Constructors =====
    public Dass21Question() {
    }

    public Dass21Question(String questionText, DassCategory category) {
        this.questionText = questionText;
        this.category = category;
    }

    // ===== Getters & Setters =====
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public DassCategory getCategory() {
        return category;
    }

    public void setCategory(DassCategory category) {
        this.category = category;
    }
}
