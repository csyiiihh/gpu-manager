package com.bingo.gpumanager.controller;

import com.bingo.gpumanager.annotation.AdminOnly;
import com.bingo.gpumanager.common.Result;
import com.bingo.gpumanager.entity.GpuServer;
import com.bingo.gpumanager.exception.BusinessException;
import com.bingo.gpumanager.service.GpuServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import com.bingo.gpumanager.common.Result;
import com.bingo.gpumanager.annotation.AdminOnly;
import com.bingo.gpumanager.dto.GpuServerRequest;
import jakarta.validation.Valid;


import java.util.List;

@Tag(
        name = "GPU服务器管理",
        description = "GPU服务器查询、添加、修改、删除接口"
)
@RestController
public class GpuServerController {

    private final GpuServerService gpuServerService;

    public GpuServerController(GpuServerService gpuServerService) {
        this.gpuServerService = gpuServerService;
    }

    @Operation(summary = "查询全部GPU服务器")
    @GetMapping("/servers")
    public Result<List<GpuServer>> getServers() {
        return Result.success(gpuServerService.getAllServers());
    }

    @Operation(summary = "根据ID查询GPU服务器")
    @GetMapping("/servers/{id}")
    public Result<GpuServer> getServerById(@PathVariable Long id) {
        return Result.success(gpuServerService.getServerById(id));
    }

    @Operation(summary = "新增GPU服务器，仅管理员")
    @AdminOnly
    @PostMapping("/servers")
    public Result<Void> addServer(
            @Valid @RequestBody GpuServerRequest request){

        GpuServer gpuServer = new GpuServer();

        gpuServer.setServerName(request.getServerName());
        gpuServer.setIpAddress(request.getIpAddress());
        gpuServer.setGpuModel(request.getGpuModel());
        gpuServer.setGpuCount(request.getGpuCount());
        gpuServer.setStatus(request.getStatus().name());
        gpuServer.setDescription(request.getDescription());

        gpuServerService.addServer(gpuServer);

        return Result.success("添加成功", null);
    }

    @Operation(summary = "修改GPU服务器，仅管理员")
    @AdminOnly
    @PutMapping("/servers/{id}")
    public Result<Void> updateServer(
            @PathVariable Long id,
            @RequestBody GpuServer gpuServer){

        gpuServer.setId(id);

        gpuServerService.updateServer(gpuServer);

        return Result.success("修改成功", null);
    }

    @Operation(summary = "删除GPU服务器，仅管理员")
    @AdminOnly
    @DeleteMapping("/servers/{id}")
    public Result<Void> deleteServer(
            @PathVariable Long id){

        gpuServerService.deleteServer(id);

        return Result.success("修改成功", null);
    }

}