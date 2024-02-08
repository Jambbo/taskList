package com.example.tasklist.repository;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    //Данная конфигурация позволяет нам получать из какого-то пула конекшенов конекшен и с ним взаимодействовать
    // это нужно спрингу ддля того чтобы обрабатывать транзакции(которые мы добавили на уровне сервисов)

    private final DataSource dataSource;

    public Connection getConnection(){
        return DataSourceUtils.getConnection(dataSource);
    }

}
