package com.dongneai.security;

import com.dongneai.common.exception.ErrorCode;
import com.dongneai.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** 인증·인가 실패를 공통 에러 형식(JSON)으로 응답한다. */
@Component
@RequiredArgsConstructor
public class SecurityErrorHandlers {

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPoint entryPoint() {
        return (request, response, ex) -> {
            Object attr = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
            ErrorCode code = attr instanceof ErrorCode ec ? ec : ErrorCode.UNAUTHORIZED;
            write(response, code);
        };
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> write(response, ErrorCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(code));
    }
}
