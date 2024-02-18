package com.example.tasklist.service.impl;

import com.example.tasklist.config.TestConfig;
import com.example.tasklist.domain.exception.ResourceNotFoundException;
import com.example.tasklist.domain.task.Status;
import com.example.tasklist.domain.task.Task;
import com.example.tasklist.domain.task.TaskImage;
import com.example.tasklist.repository.TaskRepository;
import com.example.tasklist.repository.UserRepository;
import com.example.tasklist.service.ImageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")// с так им профилем у нас будет использоваться конфигурация из TestConfig
@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)//Чтобы мы могли и мокито использовать
public class TaskServiceImplTest {

    @MockBean
    //мокбины используются как бины которые мы автовайрим для того чтобы их вставить в сервис который нам нужен
    private TaskRepository taskRepository; // тобиж мы хотим чтобы тест достал нам бин таксрепозитория и сдлела его моком, чтобы мы могли управлять его поведением

    @MockBean
    private ImageService imageService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    //тут использую @Autowired, так как хочу чтобы в него вставились зависимости выше которые появятся при конструкторе
    private TaskServiceImpl taskService;

    @Test
    void getById() {
        Long id = 1L;
        Task task = new Task();
        task.setId(id);
        Mockito.when(taskRepository.findById(id))
                .thenReturn(Optional.of(task));
        Task testTask = taskService.getById(id);
        Mockito.verify(taskRepository).findById(id);
        Assertions.assertEquals(task, testTask);
    }

    @Test
    void getByIdWithNotExistingId() {
        Long id = 1L;
        Mockito.when(taskRepository.findById(id))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> taskService.getById(id));
        Mockito.verify(taskRepository).findById(id);// проверка на то что был вызван метод findById() был вызван на taskRepository с ожидаемым идентификатором id
    }

    @Test
    void getAllByUserId() {
        Long userId = 1L;
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            tasks.add(new Task());
        }
        Mockito.when(taskRepository.findAllByUserId(userId))
                .thenReturn(tasks);
        List<Task> testTasks = taskService.getAllByUserId(userId);
        Mockito.verify(taskRepository).findAllByUserId(userId);
        Assertions.assertEquals(tasks, testTasks);
    }

    @Test
    void update() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("title");
        task.setDescription("description");
        task.setExpirationDate(LocalDateTime.now());
        task.setStatus(Status.DONE);
        Task testTask = taskService.update(task);
        Mockito.verify(taskRepository).save(task);
        Assertions.assertEquals(task, testTask);
    }

    @Test
    void updateWithNullStatus() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("title");
        task.setDescription("description");
        task.setExpirationDate(LocalDateTime.now());
        Task testTask = taskService.update(task);
        Mockito.verify(taskRepository).save(task);
        Assertions.assertEquals(Status.TODO, testTask.getStatus());
    }

    @Test
    void create() {
        Long userId = 1L;
        Long taskId = 1L;
        Task task = new Task();
        Mockito.doAnswer(invocation -> {
                    Task savedTask = invocation.getArgument(0);
                    savedTask.setId(taskId);
                    return savedTask;
                })
                .when(taskRepository).save(task);
        Task testTask = taskService.create(task, userId);
        Mockito.verify(taskRepository).save(task);
        Assertions.assertNotNull(testTask.getId());
        Mockito.verify(taskRepository).assignTask(userId, task.getId());
    }

    @Test
    void delete(){
        Long id = 1L;
        taskService.delete(id);
        Mockito.verify(taskRepository).deleteById(id);
    }

    @Test
    void uploadImage(){
        Long id = 1L;
        String imageName = "imageName";
        TaskImage taskImage = new TaskImage();
        Mockito.when(imageService.upload(taskImage))
                .thenReturn(imageName);
        taskService.uploadImage(id,taskImage);
        Mockito.verify(taskRepository).addImage(id,imageName);
    }
}
