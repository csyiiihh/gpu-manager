package com.bingo.gpumanager.interceptor;

import com.bingo.gpumanager.enums.UserRole;
import com.bingo.gpumanager.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.bingo.gpumanager.annotation.AdminOnly;
import org.springframework.web.method.HandlerMethod;
import com.bingo.gpumanager.enums.UserRole;

import java.io.IOException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler
    ) throws IOException {

        // 放行 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");

        if(authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(401);

            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write("""
                    {
                      "code": 401,
                      "message": "未登录或Token无效",
                      "data": null
                    }
                    """);

            return false;
        }

        String token = authorization.substring("Bearer ".length());

        try{

            Claims claims = jwtUtil.parseToken(token);

            Long userId = claims.get("userId", Long.class);

            String username = claims.getSubject();

            String role = claims.get("role", String.class);


            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            if(handler instanceof HandlerMethod handlerMethod){

                AdminOnly adminOnly = handlerMethod.getMethodAnnotation(AdminOnly.class);

                if(adminOnly != null && !UserRole.ADMIN.name().equals(role)){

                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");

                    response.getWriter().write("""
                            {
                                "code": 403,
                                "message": "无管理员权限",
                                "data": null
                            }
                            """);

                    return false;
                }
            }

            return true;
        }catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}