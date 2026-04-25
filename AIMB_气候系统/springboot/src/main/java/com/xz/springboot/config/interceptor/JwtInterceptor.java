package com.xz.springboot.config.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.xz.springboot.common.Constants;
import com.xz.springboot.entity.User;
import com.xz.springboot.exception.ServiceException;
import com.xz.springboot.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. Get the value from the "Authorization" header
        String authHeader = request.getHeader("Authorization");
        String uri = request.getRequestURI();
        if (uri.startsWith("/enso/")) {
            return true;
        }

        // If not mapped to a method, pass directly
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 2. Validate the Authorization header and extract the token
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new ServiceException(Constants.CODE_401, "无token或token格式不正确，请重新登录");
        }

        // 3. Get the token string (remove "Bearer ")
        String token = authHeader.substring(7);

        // --- The rest of your existing code is correct ---

        // Get user id from token
        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0);
        } catch (JWTDecodeException j) {
            throw new ServiceException(Constants.CODE_401, "token验证失败，请重新登录");
        }

        // Find user in the database
        User user = userService.getById(userId);
        if (user == null) {
            throw new ServiceException(Constants.CODE_401, "用户不存在，请重新登录");
        }

        // Verify the token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try {
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new ServiceException(Constants.CODE_401, "token验证失败，请重新登录");
        }

        return true;
    }
}