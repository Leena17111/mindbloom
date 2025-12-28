package com.mindbloom.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "mental_health_resources")
public class MentalHealthResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 255)
    private String title;

    // Short summary shown on cards & tables
    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 20)
    private String type; // VIDEO or ARTICLE

    // YouTube URL OR full article text
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Image shown on student dashboard cards
    @Column(name = "image_url")
    private String imageUrl;


    /* ===========================
       CREATED BY (TEMPORARY)
       =========================== */

    // TODO: Replace with real counselor ID when Spring Security is implemented
    @Column(name = "created_by_id", nullable = false)
    private int createdById;

     // TODO: Replace with real counselor name when Spring Security is implemented
    // Display name shown to students (e.g. "Dr. Azmina Ahmed")
    @Column(name = "created_by_name", nullable = false, length = 100)
    private String createdByName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getCreatedById() { return createdById; }
    public void setCreatedById(int createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


}
