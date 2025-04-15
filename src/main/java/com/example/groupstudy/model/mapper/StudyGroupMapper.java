package com.example.groupstudy.model.mapper;

import com.example.groupstudy.model.entity.StudyGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StudyGroupMapper {
    StudyGroup findById(Integer groupId);
    List<StudyGroup> findAll();
    List<StudyGroup> findByUserId(Integer userId);
    void insert(StudyGroup group);
    void update(StudyGroup group);
    void delete(Integer groupId);
}