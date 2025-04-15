package com.example.groupstudy.service;

import com.example.groupstudy.model.entity.GroupMember;
import com.example.groupstudy.model.entity.StudyGroup;
import com.example.groupstudy.model.mapper.GroupMemberMapper;
import com.example.groupstudy.model.mapper.StudyGroupMapper;
import com.example.groupstudy.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudyGroupService {

    private final StudyGroupMapper studyGroupMapper;
    private final GroupMemberMapper groupMemberMapper;

    @Autowired
    public StudyGroupService(StudyGroupMapper studyGroupMapper, GroupMemberMapper groupMemberMapper) {
        this.studyGroupMapper = studyGroupMapper;
        this.groupMemberMapper = groupMemberMapper;
    }

    public StudyGroup findById(Integer groupId) {
        return studyGroupMapper.findById(groupId);
    }

    public List<StudyGroup> findAll() {
        return studyGroupMapper.findAll();
    }

    public List<StudyGroup> findByUserId(Integer userId) {
        return studyGroupMapper.findByUserId(userId);
    }

    @Transactional
    public void createGroup(StudyGroup group, User creator) {
        // 그룹 생성
        group.setCreatedBy(creator.getUserId());
        studyGroupMapper.insert(group);

        // 생성자를 그룹의 관리자로 추가
        GroupMember member = new GroupMember();
        member.setGroupId(group.getGroupId());
        member.setUserId(creator.getUserId());
        member.setRole("ADMIN"); // 그룹 생성자는 관리자 역할

        groupMemberMapper.insert(member);
    }

    public void updateGroup(StudyGroup group) {
        studyGroupMapper.update(group);
    }

    @Transactional
    public void deleteGroup(Integer groupId) {
        // 그룹 멤버 먼저 삭제 (외래 키 제약조건 때문)
        List<GroupMember> members = groupMemberMapper.findByGroupId(groupId);
        for (GroupMember member : members) {
            groupMemberMapper.delete(member.getMemberId());
        }

        // 그룹 삭제
        studyGroupMapper.delete(groupId);
    }

    // 사용자가 그룹의 멤버인지 확인
    public boolean isUserGroupMember(Integer userId, Integer groupId) {
        return groupMemberMapper.findByGroupIdAndUserId(groupId, userId) != null;
    }

    // 사용자가 그룹의 관리자인지 확인
    public boolean isUserGroupAdmin(Integer userId, Integer groupId) {
        GroupMember member = groupMemberMapper.findByGroupIdAndUserId(groupId, userId);
        return member != null && "ADMIN".equals(member.getRole());
    }
}
