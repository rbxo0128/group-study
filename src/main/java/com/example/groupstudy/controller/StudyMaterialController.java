package com.example.groupstudy.controller;

import com.example.groupstudy.model.entity.StudyGroup;
import com.example.groupstudy.model.entity.StudyMaterial;
import com.example.groupstudy.model.entity.User;
import com.example.groupstudy.service.StudyGroupService;
import com.example.groupstudy.service.StudyMaterialService;
import com.example.groupstudy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/materials")
public class StudyMaterialController {

    private final StudyMaterialService studyMaterialService;
    private final StudyGroupService studyGroupService;
    private final UserService userService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired
    public StudyMaterialController(StudyMaterialService studyMaterialService,
                                   StudyGroupService studyGroupService,
                                   UserService userService) {
        this.studyMaterialService = studyMaterialService;
        this.studyGroupService = studyGroupService;
        this.userService = userService;
    }

    // 현재 로그인한 사용자 정보 가져오기
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }

    // 그룹별 학습 자료 목록 조회
    @GetMapping("/group/{groupId}")
    public String listGroupMaterials(@PathVariable Integer groupId, Model model) {
        User currentUser = getCurrentUser();

        // 그룹 멤버가 아니면 접근 불가
        if (!studyGroupService.isUserGroupMember(currentUser.getUserId(), groupId)) {
            return "redirect:/groups?error=accessdenied";
        }

        StudyGroup group = studyGroupService.findById(groupId);
        List<StudyMaterial> materials = studyMaterialService.findByGroupId(groupId);

        model.addAttribute("group", group);
        model.addAttribute("materials", materials);
        model.addAttribute("isAdmin", studyGroupService.isUserGroupAdmin(currentUser.getUserId(), groupId));

        return "materials/list";
    }

    // 학습 자료 상세 조회
    @GetMapping("/{materialId}")
    public String viewMaterial(@PathVariable Integer materialId, Model model) {
        User currentUser = getCurrentUser();
        StudyMaterial material = studyMaterialService.findById(materialId);

        if (material == null) {
            return "redirect:/dashboard?error=notfound";
        }

        // 접근 권한 확인
        if (!studyMaterialService.canAccessMaterial(currentUser.getUserId(), materialId)) {
            return "redirect:/dashboard?error=accessdenied";
        }

        StudyGroup group = null;
        if (material.getGroupId() != null) {
            group = studyGroupService.findById(material.getGroupId());
        }

        model.addAttribute("material", material);
        model.addAttribute("group", group);
        model.addAttribute("isAdmin", group != null &&
                studyGroupService.isUserGroupAdmin(currentUser.getUserId(), group.getGroupId()));

        return "materials/view";
    }

    // 학습 자료 생성 페이지
    @GetMapping("/create")
    public String createMaterialForm(@RequestParam(required = false) Integer groupId, Model model) {
        User currentUser = getCurrentUser();

        // 그룹 ID가 제공된 경우 접근 권한 확인
        if (groupId != null && !studyGroupService.isUserGroupMember(currentUser.getUserId(), groupId)) {
            return "redirect:/groups?error=accessdenied";
        }

        StudyMaterial material = new StudyMaterial();
        if (groupId != null) {
            material.setGroupId(groupId);
            model.addAttribute("group", studyGroupService.findById(groupId));
        }

        model.addAttribute("material", material);
        return "materials/create";
    }

    // 학습 자료 생성 처리
    @PostMapping("/create")
    public String createMaterial(@ModelAttribute StudyMaterial material,
                                 @RequestParam(required = false) MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        material.setUserId(currentUser.getUserId());

        // 그룹 ID가 제공된 경우 접근 권한 확인
        if (material.getGroupId() != null &&
                !studyGroupService.isUserGroupMember(currentUser.getUserId(), material.getGroupId())) {
            return "redirect:/groups?error=accessdenied";
        }

        try {
            studyMaterialService.create(material, file);
            redirectAttributes.addFlashAttribute("message", "학습 자료가 성공적으로 생성되었습니다.");

            // 그룹 자료인 경우 그룹 자료 목록으로 리다이렉트
            if (material.getGroupId() != null) {
                return "redirect:/materials/group/" + material.getGroupId();
            } else {
                return "redirect:/dashboard";
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/materials/create";
        }
    }

    // 학습 자료 수정 페이지
    @GetMapping("/{materialId}/edit")
    public String editMaterialForm(@PathVariable Integer materialId, Model model) {
        User currentUser = getCurrentUser();
        StudyMaterial material = studyMaterialService.findById(materialId);

        if (material == null) {
            return "redirect:/dashboard?error=notfound";
        }

        // 본인 자료이거나 그룹 관리자만 수정 가능
        if (!currentUser.getUserId().equals(material.getUserId()) &&
                (material.getGroupId() == null ||
                        !studyGroupService.isUserGroupAdmin(currentUser.getUserId(), material.getGroupId()))) {
            return "redirect:/dashboard?error=accessdenied";
        }

        StudyGroup group = null;
        if (material.getGroupId() != null) {
            group = studyGroupService.findById(material.getGroupId());
        }

        model.addAttribute("material", material);
        model.addAttribute("group", group);

        return "materials/edit";
    }

    // 학습 자료 수정 처리
    @PostMapping("/{materialId}/edit")
    public String editMaterial(@PathVariable Integer materialId,
                               @ModelAttribute StudyMaterial material,
                               @RequestParam(required = false) MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        StudyMaterial existingMaterial = studyMaterialService.findById(materialId);

        if (existingMaterial == null) {
            return "redirect:/dashboard?error=notfound";
        }

        // 본인 자료이거나 그룹 관리자만 수정 가능
        if (!currentUser.getUserId().equals(existingMaterial.getUserId()) &&
                (existingMaterial.getGroupId() == null ||
                        !studyGroupService.isUserGroupAdmin(currentUser.getUserId(), existingMaterial.getGroupId()))) {
            return "redirect:/dashboard?error=accessdenied";
        }

        // 기존 값 유지
        material.setMaterialId(materialId);
        material.setUserId(existingMaterial.getUserId());
        material.setGroupId(existingMaterial.getGroupId());

        try {
            studyMaterialService.update(material, file);
            redirectAttributes.addFlashAttribute("message", "학습 자료가 성공적으로 수정되었습니다.");
            return "redirect:/materials/" + materialId;
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/materials/" + materialId + "/edit";
        }
    }

    // 학습 자료 삭제 처리
    @PostMapping("/{materialId}/delete")
    public String deleteMaterial(@PathVariable Integer materialId,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        StudyMaterial material = studyMaterialService.findById(materialId);

        if (material == null) {
            return "redirect:/dashboard?error=notfound";
        }

        // 본인 자료이거나 그룹 관리자만 삭제 가능
        if (!currentUser.getUserId().equals(material.getUserId()) &&
                (material.getGroupId() == null ||
                        !studyGroupService.isUserGroupAdmin(currentUser.getUserId(), material.getGroupId()))) {
            return "redirect:/dashboard?error=accessdenied";
        }

        Integer groupId = material.getGroupId();
        studyMaterialService.delete(materialId);

        redirectAttributes.addFlashAttribute("message", "학습 자료가 성공적으로 삭제되었습니다.");

        // 그룹 자료인 경우 그룹 자료 목록으로 리다이렉트
        if (groupId != null) {
            return "redirect:/materials/group/" + groupId;
        } else {
            return "redirect:/dashboard";
        }
    }

    // 파일 다운로드
    @GetMapping("/download/{materialId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer materialId) {
        User currentUser = getCurrentUser();
        StudyMaterial material = studyMaterialService.findById(materialId);

        if (material == null || material.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        // 접근 권한 확인
        if (!studyMaterialService.canAccessMaterial(currentUser.getUserId(), materialId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        try {
            Path filePath = Paths.get(uploadDir, material.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String originalFilename = material.getFilePath().substring(material.getFilePath().indexOf("_") + 1);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
