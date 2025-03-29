package com.jiangjing.im.app.bussiness.security.handler;

import cn.hutool.json.JSONUtil;
import com.jiangjing.im.common.ResponseVO;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 403 权限异常处理器
 */
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        PrintWriter writer = response.getWriter();
        ResponseVO<String> responseVO = new ResponseVO<>();
        responseVO.setCode(403);
        responseVO.setMessage(authException.getLocalizedMessage());
        writer.print(JSONUtil.toJsonStr(responseVO));
        writer.flush();
    }
}
