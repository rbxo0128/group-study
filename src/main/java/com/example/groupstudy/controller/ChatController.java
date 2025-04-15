package com.example.groupstudy.controller;

import com.example.groupstudy.model.dto.ChatMessageDto;
import com.example.groupstudy.model.entity.ChatMessage;
import com.example.groupstudy.model.entity.User;
import com.example.groupstudy.service.ChatService;
import com.example.groupstudy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    @Autowired
    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    // WebSocket 메시지 처리
    @MessageMapping("/chat/{groupId}")
    @SendTo("/topic/chat/{groupId}")
    public ChatMessageDto sendMessage(@DestinationVariable Integer groupId,
                                      ChatMessageDto messageDto,
                                      SimpMessageHeaderAccessor headerAccessor) {
        // 인증 정보에서 사용자 가져오기
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            return null;
        }

        User user = userService.findByUsername(principal.getName());
        if (user == null) {
            return null;
        }

        // 메시지 저장
        ChatMessage chatMessage = chatService.saveMessage(groupId, user.getUserId(), messageDto.getMessage());

        // 응답 DTO 설정
        messageDto.setUserId(user.getUserId());
        messageDto.setUsername(user.getUsername());
        messageDto.setTimestamp(chatMessage.getSentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return messageDto;
    }

    // 이전 채팅 메시지 가져오기 (REST API)
    @GetMapping("/api/chat/{groupId}/messages")
    @ResponseBody
    public List<ChatMessageDto> getMessages(@PathVariable Integer groupId, Authentication authentication) {
        if (authentication == null) {
            return Collections.emptyList();
        }

        User user = userService.findByUsername(authentication.getName());
        if (user == null || !chatService.canAccessChat(user.getUserId(), groupId)) {
            return Collections.emptyList();
        }

        List<ChatMessage> messages = chatService.getRecentMessages(groupId, 50); // 최근 50개 메시지
        Collections.reverse(messages); // 시간순 정렬 (최신 메시지가 마지막에 오도록)

        List<ChatMessageDto> messageDtos = new ArrayList<>();
        for (ChatMessage message : messages) {
            User sender = chatService.getMessageSender(message.getUserId());

            ChatMessageDto dto = new ChatMessageDto();
            dto.setGroupId(message.getGroupId());
            dto.setUserId(message.getUserId());
            dto.setUsername(sender != null ? sender.getUsername() : "알 수 없음");
            dto.setMessage(message.getMessage());
            dto.setTimestamp(message.getSentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            messageDtos.add(dto);
        }

        return messageDtos;
    }
}