package com.example.tasklist.domain.user;

import com.example.tasklist.domain.task.Task;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
@Entity
@Table(name = "users")
@Data   //т.к. редис хранит объекты которые получает в виде каких-то сериализованных данных, то необходимо, что класс сериализируемый
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String username;
    private String password;
    @Transient //означает, что это поле не будет храниться в бд
    private String passwordConfirmation;

    @Column(name="role")
    @ElementCollection(fetch = FetchType.EAGER)// тобиж он будет полностью доставать всего юзера когда мы будем к нему обращаться
    @CollectionTable(name="users_roles")
    @Enumerated(value=EnumType.STRING)// т.к. это енам
    private Set<Role> roles;

    @CollectionTable(name="users_tasks")
    @OneToMany
    @JoinColumn(name="task_id")
    private List<Task> tasks;
}
