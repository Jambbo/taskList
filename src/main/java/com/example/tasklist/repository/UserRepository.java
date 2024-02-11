package com.example.tasklist.repository;

import com.example.tasklist.domain.user.User;
import com.example.tasklist.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Mapper
public interface UserRepository {

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    void update(User user);
    void create(User user);
//    Когда два входных параметра, то уже нужно указывать аннотацию @Param
    void insertUserRole(@Param("userId") Long userId,@Param("role") Role role);
    boolean isTaskOwner(@Param("userId")Long userId,@Param("taskId") Long taskId);
    void delete(Long id);

}
