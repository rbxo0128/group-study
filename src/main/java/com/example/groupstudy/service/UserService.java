package com.example.groupstudy.service;

import com.example.groupstudy.model.entity.User;
import com.example.groupstudy.model.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User findById(Integer userId) {
        return userMapper.findById(userId);
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    public void register(User user) {
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 기본 역할 설정 (만약 지정되지 않았다면)
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        userMapper.insert(user);
    }

    public void update(User user) {
        // 기존 사용자 정보 가져오기
        User existingUser = userMapper.findById(user.getUserId());

        // 비밀번호가 변경되었는지 확인 (빈 문자열이 아니고 기존과 다른 경우)
        if (user.getPassword() != null && !user.getPassword().isEmpty()
                && !passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 비밀번호가 변경되지 않았으면 기존 암호화된 비밀번호 유지
            user.setPassword(existingUser.getPassword());
        }

        userMapper.update(user);
    }

    public void delete(Integer userId) {
        userMapper.delete(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}
