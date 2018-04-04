package ru.bpirate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Stef6 on 03/15/2018.
 */
@Configuration
public class Config {
    @Bean
    public FileDAO fileDAO() {
        return new FileDAO();
    }

    @Bean
    public ResourceDAO resourceDAO() {
        return new ResourceDAO();
    }

}
