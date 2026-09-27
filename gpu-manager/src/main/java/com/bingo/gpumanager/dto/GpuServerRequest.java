package com.bingo.gpumanager.dto;

import com.bingo.gpumanager.enums.ServerStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GpuServerRequest {

    @NotBlank(message = "服务器名称不能为空")
    private String serverName;

    private String ipAddress;

    @NotBlank(message = "GPU型号不能为空")
    private String gpuModel;

    @NotNull(message = "GPU数量不能为空")
    @Min(value = 1, message = "GPU数量必须大于等于1")
    private Integer gpuCount;

    @NotNull(message = "服务器状态不能为空")
    private ServerStatus status;

    private String description;
}