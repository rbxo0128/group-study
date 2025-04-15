package com.example.groupstudy.model.mapper;

import com.example.groupstudy.model.entity.StudyMaterial;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StudyMaterialMapper {
    StudyMaterial findById(Integer materialId);
    List<StudyMaterial> findByUserId(Integer userId);
    List<StudyMaterial> findByGroupId(Integer groupId); // 그룹 ID로 조회하는 메서드 추가
    List<StudyMaterial> findByCategory(String category);
    List<StudyMaterial> findAll();
    void insert(StudyMaterial material);
    void update(StudyMaterial material);
    void delete(Integer materialId);
}