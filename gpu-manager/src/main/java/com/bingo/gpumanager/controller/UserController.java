package com.bingo.gpumanager.controller;

import com.bingo.gpumanager.common.Result;
import com.bingo.gpumanager.dto.LoginRequest;
import com.bingo.gpumanager.service.UserService;
import org.springframework.web.bind.annotation.*;
import com.bingo.gpumanager.util.JwtUtil;
import com.bingo.gpumanager.dto.RegisterRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.HashMap;
import java.util.Map;

import java.util.HashMap;
import java.util.Map;

@Tag(
        name = "用户管理",
        description = "用户注册、登录及个人信息接口"
)
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(
            @Valid @RequestBody LoginRequest request) {

        return Result.success(
                userService.login(request)
        );
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<Map<String, Object>> me(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("username") String username,
            @RequestAttribute("role") String role) {
//
//        String token = authorization.replace("Bearer ", "");
//
//        Long userId = JwtUtil.getUserId(token);
//        String username = JwtUtil.getUsername(token);

        Map<String, Object> data = new HashMap<>();

        data.put("userId", userId);
        data.put("username", username);
        data.put("role", role);

        return Result.success(data);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(
            @Valid @RequestBody RegisterRequest request) {

        userService.register(request);

        return Result.success("注册成功" ,null);
    }
}