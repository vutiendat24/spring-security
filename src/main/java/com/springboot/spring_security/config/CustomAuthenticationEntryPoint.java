package com.springboot.spring_security.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.springboot.spring_security.DTO.res.APIResponse;
import com.springboot.spring_security.exception.ErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;



// file này dùng để xử lý khi không có token hoặc token không hợp lệ
// mặc định khi lỗi 401 thì springboot nó sẽ trả về 1 trang trắng
// nhưng ta muốn nó trả về lỗi 401 Unauthorized trong api 
// do đó ta cần lớp này để cấu hình về api trả về khi có lỗi 401 
// 

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint{
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
     throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        APIResponse<?> apiResponse = APIResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
        
    }
}
