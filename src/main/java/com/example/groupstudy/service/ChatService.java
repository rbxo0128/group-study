package com.example.groupstudy.service;

import com.example.groupstudy.model.entity.ChatMessage;
import com.example.groupstudy.model.entity.User;
import com.example.groupstudy.model.mapper.ChatMessageMapper;
import com.example.groupstudy.model.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final StudyGroupService studyGroupService;

    @Autowired
    public ChatService(ChatMessageMapper chatMessageMapper, UserMapper userMapper, StudyGroupService studyGroupService) {
        this.chatMessageMapper = chatMessageMapper;
        this.userMapper = userMapper;
        this.studyGroupService = studyGroupService;
    }

    public ChatMessage saveMessage(Integer groupId, Integer userId, String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setGroupId(groupId);
        chatMessage.setUserId(userId);
        chatMessage.setMessage(message);
        chatMessage.setSentAt(LocalDateTime.now());

        chatMessageMapper.insert(chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> getRecentMessages(Integer groupId, Integer limit) {
        return chatMessageMapper.findByGroupIdWithLimit(groupId, limit);
    }

    public List<ChatMessage> getAllMessages(Integer groupId) {
        return chatMessageMapper.findByGroupId(groupId);
    }

    public boolean canAccessChat(Integer userId, Integer groupId) {
        return studyGroupService.isUserGroupMember(userId, groupId);
    }

    public User getMessageSender(Integer userId) {
        return userMapper.findById(userId);
    }
}