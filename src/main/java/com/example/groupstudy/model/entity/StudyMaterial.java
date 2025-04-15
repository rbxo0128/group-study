package com.example.groupstudy.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudyMaterial {
    private Integer materialId;
    private String title;
    private String description;
    private String content;
    private String category;
    private String filePath;
    private Integer userId;
    private Integer groupId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}