package com.dalmeng.convention.common.exception

import com.dalmeng.convention.common.response.BaseResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(e: BaseException): ResponseEntity<BaseResponse<Nothing>> {
        return ResponseEntity
            .status(e.statusCode)
            .body(
                BaseResponse.error(
                    statusCode = e.statusCode,
                    message = e.message ?: e.toString()
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(e: Exception): ResponseEntity<BaseResponse<Nothing>> {
        return ResponseEntity
            .status(500)
            .body(
                BaseResponse.error(
                    statusCode = 500,
                    message = e.message ?: e.toString(),
                )
            )
    }
}
