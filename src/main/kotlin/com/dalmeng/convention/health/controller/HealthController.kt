package com.dalmeng.convention.todo.controller

import com.dalmeng.convention.common.response.BaseResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class HealthController {

    @GetMapping
    fun health(): BaseResponse<Nothing> {
        return BaseResponse.ok()
    }
}
