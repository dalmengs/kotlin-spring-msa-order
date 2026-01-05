package com.dalmeng.convention.order.exception

import com.dalmeng.convention.common.exception.BaseException

class OrderNotFoundException : BaseException(
    statusCode = 404,
    message = "Order not found"
)

