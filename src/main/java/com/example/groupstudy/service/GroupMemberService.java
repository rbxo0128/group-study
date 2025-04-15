package com.example.groupstudy.service;

import com.example.groupstudy.model.entity.GroupMember;
import com.example.groupstudy.model.entity.User;
import com.example.groupstudy.model.mapper.GroupMemberMapper;
import com.example.groupstudy.model.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupMemberService {

    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;

    @Autowired
    public GroupMemberService(GroupMemberMapper groupMemberMapper, UserMapper userMapper) {
        this.groupMemberMapper = groupMemberMapper;
        this.userMapper = userMapper;
    }

    public List<GroupMember> getGroupMembers(Integer groupId) {
        return groupMemberMapper.findByGroupId(groupId);
    }

    public List<GroupMember> getUserGroups(Integer userId) {
        return groupMemberMapper.findByUserId(userId);
    }

    public GroupMember getMembership(Integer groupId, Integer userId) {
        return groupMemberMapper.findByGroupIdAndUserId(groupId, userId);
    }

    public void addMember(Integer groupId, Integer userId, String role) {
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(role != null ? role : "MEMBER"); // 기본 역할은 MEMBER

        groupMemberMapper.insert(member);
    }

    public void updateMemberRole(Integer memberId, String role) {
        GroupMember member = groupMemberMapper.findById(memberId);
        if (member != null) {
            member.setRole(role);
            groupMemberMapper.update(member);
        }
    }

    public void removeMember(Integer memberId) {
        groupMemberMapper.delete(memberId);
    }

    public void removeMember(Integer groupId, Integer userId) {
        groupMemberMapper.deleteByGroupIdAndUserId(groupId, userId);
    }

    // 그룹에 속한 사용자 목록 조회
    public List<User> getGroupUsers(Integer groupId) {
        // 이 메서드는 별도의 조인 쿼리가 필요합니다. 현재 매퍼에는 없으므로 추가해야 합니다.
        // 임시로 직접 조회하도록 구현합니다.
        List<GroupMember> members = groupMemberMapper.findByGroupId(groupId);
        List<User> users = new ArrayList<>();

        for (GroupMember member : members) {
            User user = userMapper.findById(member.getUserId());
            if (user != null) {
                users.add(user);
            }
        }

        return users;
    }
}