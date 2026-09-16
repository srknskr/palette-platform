package com.serkanmusic.palette.shared.error

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(ex.status, ex.message)
        problem.type = URI.create("about:blank")
        problem.title = ex.status.reasonPhrase
        return problem
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed")
        problem.type = URI.create("about:blank")
        problem.title = "Bad Request"

        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = if (error is FieldError) error.field else error.objectName
            val message = error.defaultMessage ?: "Invalid value"
            fieldName to message
        }
        problem.setProperty("errors", errors)
        return problem
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(@Suppress("UnusedParameter") ex: BadCredentialsException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        problem.type = URI.create("about:blank")
        problem.title = "Unauthorized"
        return problem
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.message ?: "Authentication required")
        problem.type = URI.create("about:blank")
        problem.title = "Unauthorized"
        return problem
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(@Suppress("UnusedParameter") ex: AccessDeniedException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied")
        problem.type = URI.create("about:blank")
        problem.title = "Forbidden"
        return problem
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request")
        problem.type = URI.create("about:blank")
        problem.title = "Bad Request"
        return problem
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(@Suppress("UnusedParameter") ex: Exception): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
        problem.type = URI.create("about:blank")
        problem.title = "Internal Server Error"
        return problem
    }
}
