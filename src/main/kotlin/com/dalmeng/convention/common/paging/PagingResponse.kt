package com.dalmeng.convention.common.paging

data class PagingResponse<T>(
    val items: List<T>,
    val page: Int?,
    val limit: Int,
    val nextCursor: String?,
    val prevCursor: String?,
    val hasNext: Boolean?,
    val hasPrev: Boolean?,
    val itemCount: Int
)