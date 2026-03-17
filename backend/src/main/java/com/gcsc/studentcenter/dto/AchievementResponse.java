package com.gcsc.studentcenter.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AchievementResponse {
    private Long id;
    private String name;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate awardDate;
    private String description;
    private String thoughts;
    private String imageUrl;
    private LocalDateTime createdAt;

    public AchievementResponse(
        Long id,
        String name,
        String category,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate awardDate,
        String description,
        String thoughts,
        String imageUrl,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.awardDate = awardDate;
        this.description = description;
        this.thoughts = thoughts;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getAwardDate() {
        return awardDate;
    }

    public String getDescription() {
        return description;
    }

    public String getThoughts() {
        return thoughts;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
