package com.palette.mobile.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PageMetadata(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

@Serializable
data class PagedList<T>(
    val items: List<T>,
    val metadata: PageMetadata
)
