package com.example.groupstudy.model.mapper;

import com.example.groupstudy.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    ChatMessage findById(Integer messageId);
    List<ChatMessage> findByGroupId(Integer groupId);
    List<ChatMessage> findByGroupIdWithLimit(Integer groupId, Integer limit);
    void insert(ChatMessage message);
    void delete(Integer messageId);
    void deleteByGroupId(Integer groupId);
}