package com.dalmeng.convention.common.paging

import org.springframework.data.domain.Pageable

interface PagingHandlers<T> {
    fun getCursorFromResponse(data: T): String
    fun findById(id: String): T
            = throw UnsupportedOperationException("Fetching Method (findById) is not defined.")
    fun findAllByOrderByIdAsc(pageable: Pageable): List<T>
            = throw UnsupportedOperationException("Fetching Method (findAllByOrderByIdAsc) is not defined.")
    fun findAllByOrderByIdDesc(pageable: Pageable): List<T>
            = throw UnsupportedOperationException("Fetching Method (findAllByOrderByIdDesc) is not defined.")
    fun findAllByIdGreaterThanOrderByIdAsc(id: String, pageable: Pageable): List<T>
            = throw UnsupportedOperationException("Fetching Method (findAllByIdGreaterThanOrderByIdAsc) is not defined.")
    fun findAllByIdLessThanOrderByIdDesc(id: String, pageable: Pageable): List<T>
            = throw UnsupportedOperationException("Fetching Method (findAllByIdSmallerThanOrderByIdDesc) is not defined.")
}
