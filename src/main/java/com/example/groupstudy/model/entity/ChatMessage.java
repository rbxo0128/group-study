package com.example.groupstudy.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Integer messageId;
    private Integer groupId;
    private Integer userId;
    private String message;
    private LocalDateTime sentAt;
}
