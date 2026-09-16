package com.serkanmusic.palette.shared.error

import org.springframework.http.HttpStatus

sealed class AppException(
    val status: HttpStatus,
    override val message: String,
    val detail: String? = null
) : RuntimeException(message)

class ResourceNotFoundException(message: String) : AppException(HttpStatus.NOT_FOUND, message)

class ConflictException(message: String) : AppException(HttpStatus.CONFLICT, message)

class BadRequestException(message: String) : AppException(HttpStatus.BAD_REQUEST, message)

class ForbiddenException(message: String) : AppException(HttpStatus.FORBIDDEN, message)

class UnauthorizedException(message: String) : AppException(HttpStatus.UNAUTHORIZED, message)

class RateLimitExceededException(message: String) : AppException(HttpStatus.TOO_MANY_REQUESTS, message)
