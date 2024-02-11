package com.example.tasklist.repository;

import com.example.tasklist.domain.task.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
@Mapper
public interface TaskRepository {

    Optional<Task> findById(Long id);
    List<Task> findAllByUserId(Long userId);
    //с помощью @Param, mybatis понимает какой из них какой, просто обращается к таксайди и юзерайди
    void assignToUserById(@Param("taskId") Long taskId,@Param("userId")  Long userId);
    void update(Task task);
    void create(Task task);
    void delete(Long id);

}
