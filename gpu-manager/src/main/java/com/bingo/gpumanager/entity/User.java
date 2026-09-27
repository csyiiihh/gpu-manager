package com.bingo.gpumanager.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private LocalDateTime createTime;
}