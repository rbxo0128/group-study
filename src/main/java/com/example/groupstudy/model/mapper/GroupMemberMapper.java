package com.example.groupstudy.model.mapper;

import com.example.groupstudy.model.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupMemberMapper {
    GroupMember findById(Integer memberId);
    List<GroupMember> findByGroupId(Integer groupId);
    List<GroupMember> findByUserId(Integer userId);
    GroupMember findByGroupIdAndUserId(Integer groupId, Integer userId);
    void insert(GroupMember member);
    void update(GroupMember member);
    void delete(Integer memberId);
    void deleteByGroupIdAndUserId(Integer groupId, Integer userId);
}