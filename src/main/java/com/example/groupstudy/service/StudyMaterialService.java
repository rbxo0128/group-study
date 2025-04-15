package com.example.groupstudy.service;

import com.example.groupstudy.model.entity.StudyMaterial;
import com.example.groupstudy.model.mapper.StudyMaterialMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class StudyMaterialService {

    private final StudyMaterialMapper studyMaterialMapper;
    private final StudyGroupService studyGroupService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired
    public StudyMaterialService(StudyMaterialMapper studyMaterialMapper, StudyGroupService studyGroupService) {
        this.studyMaterialMapper = studyMaterialMapper;
        this.studyGroupService = studyGroupService;
    }

    public StudyMaterial findById(Integer materialId) {
        return studyMaterialMapper.findById(materialId);
    }

    public List<StudyMaterial> findByUserId(Integer userId) {
        return studyMaterialMapper.findByUserId(userId);
    }

    public List<StudyMaterial> findByGroupId(Integer groupId) {
        return studyMaterialMapper.findByGroupId(groupId);
    }

    public List<StudyMaterial> findByCategory(String category) {
        return studyMaterialMapper.findByCategory(category);
    }

    public List<StudyMaterial> findAll() {
        return studyMaterialMapper.findAll();
    }

    @Transactional
    public void create(StudyMaterial material, MultipartFile file) throws IOException {
        // 파일 처리
        if (file != null && !file.isEmpty()) {
            String filePath = saveFile(file);
            material.setFilePath(filePath);
        }

        studyMaterialMapper.insert(material);
    }

    @Transactional
    public void update(StudyMaterial material, MultipartFile file) throws IOException {
        // 기존 자료 조회
        StudyMaterial existingMaterial = studyMaterialMapper.findById(material.getMaterialId());

        // 파일 처리
        if (file != null && !file.isEmpty()) {
            // 기존 파일이 있다면 삭제
            if (existingMaterial.getFilePath() != null) {
                deleteFile(existingMaterial.getFilePath());
            }

            // 새 파일 저장
            String filePath = saveFile(file);
            material.setFilePath(filePath);
        } else {
            // 파일이 없으면 기존 파일 경로 유지
            material.setFilePath(existingMaterial.getFilePath());
        }

        studyMaterialMapper.update(material);
    }

    @Transactional
    public void delete(Integer materialId) {
        StudyMaterial material = studyMaterialMapper.findById(materialId);

        // 파일 삭제
        if (material != null && material.getFilePath() != null) {
            deleteFile(material.getFilePath());
        }

        studyMaterialMapper.delete(materialId);
    }

    // 사용자가 해당 자료에 접근할 권한이 있는지 확인
    public boolean canAccessMaterial(Integer userId, Integer materialId) {
        StudyMaterial material = studyMaterialMapper.findById(materialId);
        if (material == null) {
            return false;
        }

        // 자료 작성자인 경우
        if (userId.equals(material.getUserId())) {
            return true;
        }

        // 그룹 멤버인 경우
        if (material.getGroupId() != null) {
            return studyGroupService.isUserGroupMember(userId, material.getGroupId());
        }

        return false;
    }

    // 파일 저장 메서드
    private String saveFile(MultipartFile file) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일명 중복 방지를 위한 UUID 사용
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // 파일 저장
        file.transferTo(filePath.toFile());

        return fileName;
    }

    // 파일 삭제 메서드
    private void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 파일 삭제 중 오류 발생 시 로깅 처리
            System.err.println("파일 삭제 중 오류 발생: " + e.getMessage());
        }
    }
}
