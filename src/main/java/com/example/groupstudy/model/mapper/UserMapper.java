package com.example.groupstudy.model.mapper;

import com.example.groupstudy.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User findById(Integer userId);
    User findByUsername(String username);
    List<User> findAll();
    void insert(User user);
    void update(User user);
    void delete(Integer userId);
}
