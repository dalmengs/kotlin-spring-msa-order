package com.dalmeng.convention.common.exception

abstract class BaseException(
    val statusCode: Int,
    message: String
) : RuntimeException(message)