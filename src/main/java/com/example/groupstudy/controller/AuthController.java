package com.example.groupstudy.controller;

import com.example.groupstudy.model.dto.UserRegistrationDto;
import com.example.groupstudy.model.entity.User;
import com.example.groupstudy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userDto") UserRegistrationDto userDto,
                               BindingResult result) {
        // 기본 검증
        if (result.hasErrors()) {
            return "auth/register";
        }

        // 사용자명 중복 확인
        if (userService.findByUsername(userDto.getUsername()) != null) {
            result.rejectValue("username", "error.username", "이미 사용 중인 사용자명입니다.");
            return "auth/register";
        }

        // DTO에서 User 엔티티로 변환
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setEmail(userDto.getEmail());
        user.setFullName(userDto.getFullName());

        // 사용자 등록
        userService.register(user);

        // 로그인 페이지로 리다이렉트
        return "redirect:/auth/login?registered";
    }
}
