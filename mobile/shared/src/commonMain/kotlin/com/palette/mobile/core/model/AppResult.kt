package com.palette.mobile.core.model

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

data class AppError(
    val message: String,
    val statusCode: Int? = null,
    val type: ErrorType = ErrorType.UNKNOWN,
    val validationErrors: Map<String, String> = emptyMap()
)

enum class ErrorType {
    NETWORK,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    VALIDATION,
    SERVER,
    UNKNOWN
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> {
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Error -> this
    }
}
