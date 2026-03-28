package com.springboot.spring_security.DTO.res;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class APIResponse<T> {
    T data;
    String message;
    int code;

    public static <T> APIResponse<T> success(T data, String message) {
        return new APIResponse<>(data, message, 200);
    }

    public static <T> APIResponse<T> error(T data, String message) {
        return new APIResponse<>(data, message, 500);
    }
}
