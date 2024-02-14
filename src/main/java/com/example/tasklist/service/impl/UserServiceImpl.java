package com.example.tasklist.service.impl;

import com.example.tasklist.domain.user.Role;
import com.example.tasklist.domain.user.User;
import com.example.tasklist.repository.UserRepository;
import com.example.tasklist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.tasklist.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
    @Cacheable(value="UserService::getById",key="#id") // когда мы обращаемся к программе, к этому методу, спринг проверяет есть ли в кеше(редисе) по данному ключу в данном value что-нибудь, нет - запрос к бд, да - возвращаются из кеша данные
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found."));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value="UserService::getByUsername",key="#username")
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(()->new ResourceNotFoundException("User not found."));
    }

    //CachePut заменяет уже существующий кеш и так мы заменим значения(что и должен делать update())
    @Override
    @Transactional
    @Caching(put={
            @CachePut(value="UserService::getById",key="#user.id"),
            @CachePut(value="UserService::getByUsername",key="#user.username")
    })
    public User update(User user) { //тут от пользователя приходит сырой пароль и нам нужно его захешировать и будет сохранятся в базу данных
       user.setPassword(passwordEncoder.encode(user.getPassword()));
       userRepository.save(user);
       return user;
    }

    @Override
    @Transactional
    @Caching(cacheable={
            @Cacheable(value="UserService::getById",key="#user.id"),
            @Cacheable(value="UserService::getByUsername",key="#user.username")
    })
    public User create(User user) {
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new IllegalStateException("User already exists.");
        }
        if(!user.getPassword().equals(user.getPasswordConfirmation())){
            throw new IllegalStateException("Password and password confirmation do not match.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = Set.of(Role.ROLE_USER);
        user.setRoles(roles);
        userRepository.save(user);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value="UserService::isTaskOwner",key="#userId+'.'+#taskId")
    public boolean isTaskOwner(Long userId, Long taskId) {
        return userRepository.isTaskOwner(userId,taskId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "UserService::getById",key="#id")
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
