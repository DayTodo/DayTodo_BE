package com.daytodo.global.apiPayload.handler;

import com.daytodo.global.apiPayload.ErrorResponse;
import com.daytodo.global.apiPayload.code.BaseErrorCode;
import com.daytodo.global.apiPayload.code.GeneralErrorCode;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GeneralExceptionAdvice {

    // 프로젝트 커스텀 예외 처리
    @ExceptionHandler(ProjectException.class)
    public ResponseEntity<ErrorResponse> handleProjectException(
            ProjectException e
    ) {
        BaseErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // @Valid 어노테이션 검증 실패 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        // 검증 실패한 필드별 메시지를 하나로 합쳐 message에 담는다.
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        BaseErrorCode code = GeneralErrorCode.VALIDATION_ERROR;
        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.of(code, message));
    }

    // 매핑되지 않은 경로 요청 예외
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException e
    ) {
        BaseErrorCode code = GeneralErrorCode.NOT_FOUND;
        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.of(code));
    }

    // 그 외에 정의되지 않은 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex
    ) {
        BaseErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.of(code, ex.getMessage()));
    }
}
