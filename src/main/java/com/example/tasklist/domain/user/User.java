package com.example.tasklist.domain.user;

import com.example.tasklist.domain.task.Task;
import jakarta.persistence.Id;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data   //т.к. редис хранит объекты которые получает в виде каких-то сериализованных данных, то необходимо, что класс сериализируемый
public class User implements Serializable {
    private Long id;
    private String name;
    private String username;
    private String password;
    private String passwordConfirmation;
    private Set<Role> roles;
    private List<Task> tasks;
}
