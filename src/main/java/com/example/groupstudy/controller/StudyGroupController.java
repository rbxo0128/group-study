package com.example.groupstudy.controller;

import com.example.groupstudy.model.entity.GroupMember;
import com.example.groupstudy.model.entity.StudyGroup;
import com.example.groupstudy.model.entity.User;
import com.example.groupstudy.service.GroupMemberService;
import com.example.groupstudy.service.StudyGroupService;
import com.example.groupstudy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final GroupMemberService groupMemberService;
    private final UserService userService;

    @Autowired
    public StudyGroupController(StudyGroupService studyGroupService,
                                GroupMemberService groupMemberService,
                                UserService userService) {
        this.studyGroupService = studyGroupService;
        this.groupMemberService = groupMemberService;
        this.userService = userService;
    }

    // 현재 로그인한 사용자 정보 가져오기
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }

    // 그룹 목록 페이지
    @GetMapping
    public String listGroups(Model model) {
        User currentUser = getCurrentUser();
        List<StudyGroup> userGroups = studyGroupService.findByUserId(currentUser.getUserId());
        List<StudyGroup> allGroups = studyGroupService.findAll();

        model.addAttribute("userGroups", userGroups);
        model.addAttribute("allGroups", allGroups);
        return "groups/list";
    }

    // 그룹 상세 페이지
    @GetMapping("/{groupId}")
    public String viewGroup(@PathVariable Integer groupId, Model model) {
        User currentUser = getCurrentUser();
        StudyGroup group = studyGroupService.findById(groupId);

        if (group == null) {
            return "redirect:/groups?error=notfound";
        }

        // 그룹 멤버가 아니면 접근 불가
        if (!studyGroupService.isUserGroupMember(currentUser.getUserId(), groupId)) {
            return "redirect:/groups?error=accessdenied";
        }

        List<User> members = groupMemberService.getGroupUsers(groupId);
        boolean isAdmin = studyGroupService.isUserGroupAdmin(currentUser.getUserId(), groupId);

        model.addAttribute("group", group);
        model.addAttribute("members", members);
        model.addAttribute("isAdmin", isAdmin);

        return "groups/view";
    }

    // 그룹 생성 페이지
    @GetMapping("/create")
    public String createGroupForm(Model model) {
        model.addAttribute("group", new StudyGroup());
        return "groups/create";
    }

    // 그룹 생성 처리
    @PostMapping("/create")
    public String createGroup(@ModelAttribute StudyGroup group, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        studyGroupService.createGroup(group, currentUser);

        redirectAttributes.addFlashAttribute("message", "그룹이 성공적으로 생성되었습니다.");
        return "redirect:/groups";
    }

    // 그룹 수정 페이지
    @GetMapping("/{groupId}/edit")
    public String editGroupForm(@PathVariable Integer groupId, Model model) {
        User currentUser = getCurrentUser();
        StudyGroup group = studyGroupService.findById(groupId);

        if (group == null) {
            return "redirect:/groups?error=notfound";
        }

        // 관리자만 수정 가능
        if (!studyGroupService.isUserGroupAdmin(currentUser.getUserId(), groupId)) {
            return "redirect:/groups?error=accessdenied";
        }

        model.addAttribute("group", group);
        return "groups/edit";
    }

    // 그룹 수정 처리
    @PostMapping("/{groupId}/edit")
    public String editGroup(@PathVariable Integer groupId,
                            @ModelAttribute StudyGroup group,
                            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();

        // 관리자만 수정 가능
        if (!studyGroupService.isUserGroupAdmin(currentUser.getUserId(), groupId)) {
            return "redirect:/groups?error=accessdenied";
        }

        group.setGroupId(groupId); // ID 설정 확인
        studyGroupService.updateGroup(group);

        redirectAttributes.addFlashAttribute("message", "그룹이 성공적으로 수정되었습니다.");
        return "redirect:/groups/" + groupId;
    }

    // 그룹 삭제 처리
    @PostMapping("/{groupId}/delete")
    public String deleteGroup(@PathVariable Integer groupId, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();

        // 관리자만 삭제 가능
        if (!studyGroupService.isUserGroupAdmin(currentUser.getUserId(), groupId)) {
            return "redirect:/groups?error=accessdenied";
        }

        studyGroupService.deleteGroup(groupId);

        redirectAttributes.addFlashAttribute("message", "그룹이 성공적으로 삭제되었습니다.");
        return "redirect:/groups";
    }

    @PostMapping("/{groupId}/join")
    public String joinGroup(@PathVariable Integer groupId, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();

        // 이미 그룹 멤버인지 확인
        if (studyGroupService.isUserGroupMember(currentUser.getUserId(), groupId)) {
            redirectAttributes.addFlashAttribute("error", "이미 그룹에 가입되어 있습니다.");
            return "redirect:/groups/" + groupId;
        }

        // 멤버로 추가
        groupMemberService.addMember(groupId, currentUser.getUserId(), "MEMBER");

        redirectAttributes.addFlashAttribute("message", "그룹에 성공적으로 가입되었습니다.");
        return "redirect:/groups/" + groupId;
    }

    // 그룹 탈퇴 처리
    @PostMapping("/{groupId}/leave")
    public String leaveGroup(@PathVariable Integer groupId, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();

        // 그룹 관리자인 경우 탈퇴 불가
        if (studyGroupService.isUserGroupAdmin(currentUser.getUserId(), groupId)) {
            redirectAttributes.addFlashAttribute("error", "그룹 관리자는 탈퇴할 수 없습니다. 관리자 권한을 다른 사용자에게 이전하거나 그룹을 삭제하세요.");
            return "redirect:/groups/" + groupId;
        }

        // 그룹 멤버가 아닌 경우
        if (!studyGroupService.isUserGroupMember(currentUser.getUserId(), groupId)) {
            redirectAttributes.addFlashAttribute("error", "그룹 멤버가 아닙니다.");
            return "redirect:/groups";
        }

        // 그룹 탈퇴 처리
        groupMemberService.removeMember(groupId, currentUser.getUserId());

        redirectAttributes.addFlashAttribute("message", "그룹에서 탈퇴했습니다.");
        return "redirect:/groups";
    }

    // 그룹 멤버 관리 페이지
    @GetMapping("/{groupId}/members")
    public String manageMembers(@PathVariable Integer groupId, Model model) {
        User currentUser = getCurrentUser();

        // 관리자만 멤버 관리 가능
        if (!studyGroupService.isUserGroupAdmin(currentUser.getUserId(), groupId)) {
            return "redirect:/groups/" + groupId + "?error=accessdenied";
        }

        StudyGroup group = studyGroupService.findById(groupId);
        List<User> members = groupMemberService.getGroupUsers(groupId);
        List<GroupMember> memberships = groupMemberService.getGroupMembers(groupId);

        model.addAttribute("group", group);
        model.addAttribute("members", members);
        model.addAttribute("memberships", memberships);

        return "groups/members";
    }

    // 그룹 멤버 역할 변경
    @PostMapping("/{groupId}/members/{memberId}/role")
    public String changeMemberRole(@PathVariable Integer groupId,
                                   @PathVariable Integer memberId,
                                   @RequestParam String role,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();

        // 관리자만 역할 변경 가능
        if (!studyGroupService.isUserGroupAdmin(currentUser.getUserId(), groupId)) {
            return "redirect:/groups/" + groupId + "?error=accessdenied";
        }

        groupMemberService.updateMemberRole(memberId, role);

        redirectAttributes.addFlashAttribute("message", "멤버 역할이 변경되었습니다.");
        return "redirect:/groups/" + groupId + "/members";
    }

    // 그룹 멤버 삭제
    @PostMapping("/{groupId}/members/{memberId}/remove")
    public String removeMember(@PathVariable Integer groupId,
                               @PathVariable Integer memberId,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();

        // 관리자만 멤버 삭제 가능
        if (!studyGroupService.isUserGroupAdmin(currentUser.getUserId(), groupId)) {
            return "redirect:/groups/" + groupId + "?error=accessdenied";
        }

        groupMemberService.removeMember(memberId);

        redirectAttributes.addFlashAttribute("message", "멤버가 그룹에서 삭제되었습니다.");
        return "redirect:/groups/" + groupId + "/members";
    }
}
