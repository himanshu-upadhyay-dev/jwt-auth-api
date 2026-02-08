package com.upadhyay.jwtauth.dto.common;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private String httpStatus;
    private int httpStatusCode;
    private String code;
    private T data;
    private List<ErrorDetail> errors;
    private Pagination pagination;
    private Map<String, Object> metadata;
    private String traceId;
    private String path;
    private String apiVersion;
    private Instant timestamp;

    // ---- Static factory methods (easy to use) ----

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .httpStatus(HttpStatus.OK.name())
                .httpStatusCode(HttpStatus.OK.value())
                .data(data)
                .apiVersion("v1")
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .httpStatus(HttpStatus.CREATED.name())
                .httpStatusCode(HttpStatus.CREATED.value())
                .data(data)
                .apiVersion("v1")
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .httpStatus(status.name())
                .httpStatusCode(status.value())
                .code(code)
                .apiVersion("v1")
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status, String code, String path, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .httpStatus(status.name())
                .httpStatusCode(status.value())
                .code(code)
                .path(path)
                .traceId(traceId)
                .apiVersion("v1")
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> validationError(String message, List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .httpStatus(HttpStatus.BAD_REQUEST.name())
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .code("VALIDATION_FAILED")
                .errors(errors)
                .apiVersion("v1")
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> paginated(T data, Pagination pagination, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .httpStatus(HttpStatus.OK.name())
                .httpStatusCode(HttpStatus.OK.value())
                .data(data)
                .pagination(pagination)
                .apiVersion("v1")
                .timestamp(Instant.now())
                .build();
    }
}
