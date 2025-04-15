package com.example.groupstudy.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupMember {
    private Integer memberId;
    private Integer groupId;
    private Integer userId;
    private String role;
    private LocalDateTime joinedAt;
}