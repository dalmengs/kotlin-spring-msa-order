package com.dalmeng.convention.common.paging

import com.dalmeng.convention.common.exception.InvalidPageRequestException

data class BasePagingRequest(
    val limit: Int = 20,
    val page: Int? = null,
    val cursor: String? = null,
    val direction: PagingDirection = PagingDirection.DOWN
) {
    fun validate() {
        if (page != null && direction == PagingDirection.BIDIRECTIONAL) {
            throw InvalidPageRequestException("Bidirectional paging is not allowed with page.")
        }

        if (page != null && cursor != null) {
            throw InvalidPageRequestException("Page and cursor cannot be used together.")
        }

        if (direction == PagingDirection.BIDIRECTIONAL && cursor == null) {
            throw InvalidPageRequestException("Bidirectional paging requires cursor.")
        }
    }
}
