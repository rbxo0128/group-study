package com.example.groupstudy.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudyGroup {
    private Integer groupId;
    private String name;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}