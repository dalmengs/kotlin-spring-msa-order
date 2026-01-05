package com.dalmeng.convention.common.exception

class InvalidPageRequestException(
    message: String = "Invalid paging request"
) : BaseException(
    statusCode = 400,
    message = message
)