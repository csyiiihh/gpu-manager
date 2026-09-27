package com.bingo.gpumanager.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationDTO {

    private Long id;

    private Long userId;
    private String username;
    private String name;

    private Long serverId;
    private String serverName;
    private String gpuModel;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;
    private LocalDateTime createTime;
}