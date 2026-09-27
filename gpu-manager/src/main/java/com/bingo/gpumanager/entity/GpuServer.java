package com.bingo.gpumanager.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GpuServer {

    private Long id;
    private String serverName;
    private String ipAddress;
    private String gpuModel;
    private Integer gpuCount;
    private String status;
    private String description;
    private LocalDateTime createTime;
}
