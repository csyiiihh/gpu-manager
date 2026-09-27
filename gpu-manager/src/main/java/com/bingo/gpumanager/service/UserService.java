package com.bingo.gpumanager.service;

import com.bingo.gpumanager.dto.LoginRequest;
import com.bingo.gpumanager.dto.RegisterRequest;
import com.bingo.gpumanager.entity.User;
import com.bingo.gpumanager.enums.UserRole;
import com.bingo.gpumanager.exception.BusinessException;
import com.bingo.gpumanager.mapper.UserMapper;
import com.bingo.gpumanager.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private final JwtUtil jwtUtil;

    public UserService(
            UserMapper userMapper,
            JwtUtil jwtUtil) {

        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    public String login(LoginRequest request) {

        User user =
                userMapper.findByUsername(request.getUsername());

        if (user == null) {
            throw new BusinessException(
                    401,
                    "用户名或密码错误"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new BusinessException(
                    401,
                    "用户名或密码错误"
            );
        }

        return jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }

    public void register(RegisterRequest request) {

        User existUser =
                userMapper.findByUsername(request.getUsername());

        if (existUser != null) {
            throw new BusinessException(
                    409,
                    "用户名已存在"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.getPassword()
                );

        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setName(request.getName());
        user.setRole(UserRole.USER.name());

        int result =
                userMapper.insert(user);

        if (result == 0) {
            throw new BusinessException(
                    500,
                    "注册失败"
            );
        }
    }
}