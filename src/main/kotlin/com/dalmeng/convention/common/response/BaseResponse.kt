package com.dalmeng.convention.common.response

data class BaseResponse<T>(
    val statusCode: Int,
    val message: String,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> ok(data: T? = null, statusCode: Int = 200, message: String = "Succeed"): BaseResponse<T> =
            BaseResponse(
                statusCode = statusCode,
                message = message,
                data = data
            )

        fun error(statusCode: Int, message: String = "Failed"): BaseResponse<Nothing> =
            BaseResponse(
                statusCode = statusCode,
                message = message,
                error = ErrorResponse(message)
            )
    }
}
