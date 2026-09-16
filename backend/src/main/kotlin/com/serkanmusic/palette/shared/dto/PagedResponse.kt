package com.serkanmusic.palette.shared.dto

import org.springframework.data.domain.Page

data class PageMetadata(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class PagedResponse<T>(
    val items: List<T>,
    val metadata: PageMetadata
) {
    companion object {
        fun <T> from(page: Page<T>): PagedResponse<T> {
            return PagedResponse(
                items = page.content,
                metadata = PageMetadata(
                    page = page.number,
                    size = page.size,
                    totalElements = page.totalElements,
                    totalPages = page.totalPages,
                    hasNext = page.hasNext(),
                    hasPrevious = page.hasPrevious()
                )
            )
        }
    }
}
