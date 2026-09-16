package com.serkanmusic.palette.moderation.api.dto

data class ModerationActionRequest(
    val reason: String? = null
)

data class ModerationActionResponse(
    val status: String
)
