package com.dalmeng.convention.todo.controller

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class TodoControllerTest : BehaviorSpec({
    Given("1하고 2를") {
        When("더하면") {
            Then("3이 나와야 한다.") {
                val result = 1 + 2
                result shouldBe 3
            }
        }
    }
})