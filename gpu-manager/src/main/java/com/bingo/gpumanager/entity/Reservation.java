package com.bingo.gpumanager.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Reservation {

    private Long id;

    private Long userId;

    private Long serverId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private LocalDateTime createTime;
}