package com.jiangjing.im.app.bussiness.security.filter;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.jiangjing.im.app.bussiness.security.ImUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiangjing.im.common.utils.JWTUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 排除白名单路径
        if (isSkipCheck(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 提取并验证 Token
        String token = request.getHeader("Authorization");
        if (token == null) {
            // Missing token
            throw new BadCredentialsException("token is null");
        }

        try {
            JWTValidator.of(token)
                    .validateAlgorithm(JWTSignerUtil.hs256(JWTUtil.PRIVATE_KEY));
        } catch (ValidateException e) {
            // 令牌非法，需要返回 403
            throw new BadCredentialsException("token is bad");
        }

        try {
            JWTValidator.of(token).validateDate(new Date());
        } catch (ValidateException e) {
            // 令牌失效，需要返回 401
            throw new AccessDeniedException(e.getLocalizedMessage());
        }

        // 3. 构建认证对象
        ImUserDetails principal = JSONUtil.toBean((String) JWTUtil.getPayload(token).get("principal"), ImUserDetails.class);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private boolean isSkipCheck(HttpServletRequest request) {
        return Arrays.asList(
                "/app-business/v1/login",
                "/public/**"
        ).contains(request.getRequestURI());
    }
}
