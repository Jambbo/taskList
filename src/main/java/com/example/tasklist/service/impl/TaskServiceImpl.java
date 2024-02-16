package com.example.tasklist.service.impl;

import com.example.tasklist.domain.exception.ResourceNotFoundException;
import com.example.tasklist.domain.task.Status;
import com.example.tasklist.domain.task.Task;
import com.example.tasklist.domain.task.TaskImage;
import com.example.tasklist.domain.user.User;
import com.example.tasklist.repository.TaskRepository;
import com.example.tasklist.service.ImageService;
import com.example.tasklist.service.TaskService;
import com.example.tasklist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
//    private final UserService userService;
    private final ImageService imageService;

    //Там, где гет-методы следует ставить аннотацию Transactional(readOnly=true), просто чтоб дать спрингу понять как работать с этим
    // А там где что-то меняется, то просто Transactional
    //readOnly=true следует вешать только там, где только чтение, тобиж где SELECT происходит, где изменения есть, которые могут повлиять на консистентность бд следует вешать просто Transactionsal

    //Cache! ! !
    // Редис хранит инфу в формате джсон, пары ключ-значение
//Изменил UserService таки образом, чтобы результаты гет-методов помещались в кеш,
// а результаты изменяемы методов(пост,пут) они заменяли/очищали кеш, то есть запрос в бд будет происходить
// однажды, а остальные результаты этих методов будут возвращаться из кеша, это позволит ускорить работу программы
// и это достаточно экономно, т.к. запросы в бд бывают большие
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "TaskService::getById", key = "#id")
    public Task getById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getAllByUserId(Long id) {
        return taskRepository.findAllByUserId(id);
    }

    @Override
    @Transactional
    @CachePut(value = "TaskService::getById", key = "#task.id")
    public Task update(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(Status.TODO);
        }
        taskRepository.save(task);
        return task;
    }

    @Override
    @Transactional
    @Cacheable(value = "TaskService::getById",condition = "#task.id!=null", key = "#task.id")
    public Task create(Task task, Long userId) {
     if(task.getStatus()!=null){
         task.setStatus(Status.TODO);
     }
     taskRepository.save(task);
     taskRepository.assignTask(userId,task.getId());
     return task;
    }

    @Override
    @Transactional
    @CacheEvict(value = "TaskService::getById", key = "#id")
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional //вешается эта аннотация т.к. она меняет состояние бд
    @CacheEvict(value = "TaskService::getById", key = "#id")
    public void uploadImage(Long id, TaskImage image) {
        String fileName = imageService.upload(image);
        taskRepository.addImage(id,fileName);
    }
}
