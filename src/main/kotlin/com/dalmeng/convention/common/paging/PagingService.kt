package com.dalmeng.convention.common.paging

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class PagingService {
    fun <T> paginate(
        request: BasePagingRequest,
        handlers: PagingHandlers<T>
    ): PagingResponse<T> {
        request.validate()

        val limit = request.limit

        val isPageRequest = request.page != null || request.cursor == null

        val pagingResponse: PagingResponse<T> = when (request.direction) {

            PagingDirection.BIDIRECTIONAL -> {
                val half = limit / 2
                val rest = limit - half
                val cursor = request.cursor!!

                if (limit == 1) {
                    val center = handlers.findById(cursor)

                    return PagingResponse(
                        items = listOfNotNull(center),
                        hasPrev = true,
                        hasNext = true,
                        prevCursor = handlers.getCursorFromResponse(center),
                        nextCursor = handlers.getCursorFromResponse(center),
                        limit = limit,
                        page = null,
                        itemCount = 1
                    )
                }

                val upper = handlers.findAllByIdLessThanOrderByIdDesc(
                    cursor,
                    PageRequest.of(0, half + 1, Sort.by("id").descending())
                )

                val hasPrev = upper.size > half
                val upperItems = upper.take(half).reversed()

                val lower = handlers.findAllByIdGreaterThanOrderByIdAsc(
                    cursor,
                    PageRequest.of(0, rest + 1, Sort.by("id").ascending())
                )

                val hasNext = lower.size > rest
                val lowerItems = lower.take(rest)

                val center = handlers.findById(cursor)

                val items: List<T> = upperItems + center + lowerItems

                PagingResponse(
                    items = items,
                    hasPrev = hasPrev,
                    hasNext = hasNext,
                    prevCursor = items.firstOrNull()?.let(handlers::getCursorFromResponse),
                    nextCursor = items.lastOrNull()?.let(handlers::getCursorFromResponse),
                    limit = limit,
                    page = null,
                    itemCount = items.size
                )
            }

            PagingDirection.UP -> {
                val data = if (isPageRequest) {
                    val page = request.page ?: 1
                    handlers.findAllByOrderByIdDesc(
                        PageRequest.of(
                            page - 1,
                            limit,
                            Sort.by("id").descending()
                        )
                    )
                } else {
                    handlers.findAllByIdLessThanOrderByIdDesc(
                        request.cursor,
                        PageRequest.of(0, limit + 1, Sort.by("id").descending())
                    )
                }

                val hasPrev = data.size > limit
                val items = data.take(limit)

                PagingResponse(
                    items = items.reversed(),
                    hasPrev = hasPrev,
                    hasNext = null,
                    prevCursor = items.lastOrNull()?.let(handlers::getCursorFromResponse),
                    nextCursor = null,
                    limit = limit,
                    page = if (isPageRequest) request.page else null,
                    itemCount = items.size
                )
            }

            PagingDirection.DOWN -> {
                val data = if (isPageRequest) {
                    val page = request.page ?: 1
                    handlers.findAllByOrderByIdAsc(
                        PageRequest.of(
                            page - 1,
                            limit,
                            Sort.by("id").ascending()
                        )
                    )
                } else {
                    handlers.findAllByIdGreaterThanOrderByIdAsc(
                        request.cursor,
                        PageRequest.of(0, limit + 1, Sort.by("id").ascending())
                    )
                }

                val hasNext = data.size > limit
                val items = data.take(limit)

                PagingResponse(
                    items = items,
                    hasPrev = null,
                    hasNext = hasNext,
                    prevCursor = null,
                    nextCursor = items.lastOrNull()?.let(handlers::getCursorFromResponse),
                    limit = limit,
                    page = if (isPageRequest) request.page else null,
                    itemCount = items.size
                )
            }
        }
        return pagingResponse
    }

    fun <T, E> map(
        response: PagingResponse<T>,
        mapper: (T) -> E,
    ): PagingResponse<E> {
        val items = response.items.map(mapper)
        return PagingResponse(
            items = items,
            hasPrev = response.hasPrev,
            hasNext = response.hasNext,
            prevCursor = response.prevCursor,
            nextCursor = response.nextCursor,
            limit = response.limit,
            page = response.page,
            itemCount = response.itemCount
        )
    }
}