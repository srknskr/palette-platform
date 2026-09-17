package com.palette.mobile.palette.model

import kotlinx.serialization.Serializable

@Serializable
data class Palette(
    val id: String,
    val name: String,
    val status: String,
    val likeCount: Long,
    val colors: List<String>,
    val tags: List<String>,
    val createdBy: String,
    val createdAt: String,
    val publishedAt: String?,
    val likedByMe: Boolean
)

enum class PaletteSort(val queryValue: String) {
    NEWEST("newest"),
    POPULAR("popular"),
    RANDOM("random")
}

data class PaletteFilter(
    val query: String? = null,
    val tag: String? = null,
    val hexColor: String? = null,
    val sort: PaletteSort = PaletteSort.NEWEST
)
