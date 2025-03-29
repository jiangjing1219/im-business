package com.jiangjing.im.app.bussiness.security.handler;

import cn.hutool.json.JSONUtil;
import com.jiangjing.im.common.ResponseVO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 401 访问异常
 * @author Admin
 */
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        ResponseVO<String> responseVO = new ResponseVO<>();
        responseVO.setCode(401);
        responseVO.setMessage(accessDeniedException.getLocalizedMessage());
        writer.print(JSONUtil.toJsonStr(responseVO));
        writer.flush();
    }
}
