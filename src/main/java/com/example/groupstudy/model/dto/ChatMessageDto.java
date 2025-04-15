package com.example.groupstudy.model.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private Integer groupId;
    private Integer userId;
    private String username;
    private String message;
    private String timestamp;
}
