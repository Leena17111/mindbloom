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

    /* =========================
       BASIC INFO
       ========================= */

    @Column(nullable = false, length = 255)
    private String title;

    // Short summary shown on cards & tables
    @Column(nullable = false, length = 500)
    private String description;

    // e.g. Anxiety, Depression, Stress, Burnout
    @Column(nullable = false, length = 100)
    private String category;

    // VIDEO or ARTICLE
    @Column(nullable = false, length = 20)
    private String type;

    /* =========================
       CONTENT
       ========================= */

    // Used when type = VIDEO
    @Column(name = "youtube_video_id", length = 50)
    private String youtubeVideoId;

    // Used when type = ARTICLE
    @Column(name = "article_url", length = 500)
    private String articleUrl;

    // e.g. "5 min video", "Quick read"
    @Column(name = "estimated_duration", length = 50)
    private String estimatedDuration;

    /* =========================
       CREATED BY
       ========================= */

    @Column(name = "created_by_id", nullable = false)
    private int createdById;

    @Column(name = "created_by_name", nullable = false, length = 100)
    private String createdByName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /* =========================
       GETTERS & SETTERS
       ========================= */

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

    public String getYoutubeVideoId() { return youtubeVideoId; }
    public void setYoutubeVideoId(String youtubeVideoId) { this.youtubeVideoId = youtubeVideoId; }

    public String getArticleUrl() { return articleUrl; }
    public void setArticleUrl(String articleUrl) { this.articleUrl = articleUrl; }

    public String getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public int getCreatedById() { return createdById; }
    public void setCreatedById(int createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
