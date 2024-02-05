package com.example.tasklist.web.dto.user;


import com.example.tasklist.web.dto.validation.OnCreate;
import com.example.tasklist.web.dto.validation.OnUpdate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
public class UserDto {


    //В этом классе с помощью аннотаций @NotNull и @Length ставятся ограничения
    // с помощью groups={..} определяется что и в каких случаях нужно обязательно вводить
    // Во всех тех полях, где в groups={..} указано OnCreate.class, тобиж все кроме айди, обязательно указывать значения при регистрации


    @NotNull(message = "id must be not null.", groups = OnUpdate.class)
    private Long id;

    @NotNull(message = "name must be not mull", groups = {OnUpdate.class, OnCreate.class})
    @Length(max = 255, message = "Name length must be shorter than 255 symbols", groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @NotNull(message = "username must be not mull", groups = {OnUpdate.class, OnCreate.class})
    @Length(max = 255, message = "Username length must be shorter than 255 symbols", groups = {OnCreate.class, OnUpdate.class})
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull(message = "Password must be not null.", groups = {OnCreate.class, OnUpdate.class})
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull(message = "Password confirmation must be not null.", groups = {OnCreate.class})
    private String passwordConfirmation;

}
