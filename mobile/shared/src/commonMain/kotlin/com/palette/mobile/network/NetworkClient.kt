package com.palette.mobile.network

import com.palette.mobile.auth.model.Role
import com.palette.mobile.auth.model.User
import com.palette.mobile.auth.storage.TokenStorage
import com.palette.mobile.core.model.AppError
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.core.model.ErrorType
import com.palette.mobile.core.model.PageMetadata
import com.palette.mobile.core.model.PagedList
import com.palette.mobile.network.dto.AuthResponseDto
import com.palette.mobile.network.dto.CreatePaletteRequestDto
import com.palette.mobile.network.dto.LoginRequestDto
import com.palette.mobile.network.dto.LogoutRequestDto
import com.palette.mobile.network.dto.PagedResponseDto
import com.palette.mobile.network.dto.PaletteResponseDto
import com.palette.mobile.network.dto.ProblemDetailDto
import com.palette.mobile.network.dto.RefreshRequestDto
import com.palette.mobile.network.dto.RegisterRequestDto
import com.palette.mobile.network.dto.UpdatePaletteRequestDto
import com.palette.mobile.network.dto.UserResponseDto
import com.palette.mobile.palette.model.Palette
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class NetworkClient(
    private val apiConfig: ApiConfig,
    private val tokenStorage: TokenStorage,
    private val httpClient: HttpClient = createDefaultHttpClient()
) {
    private val refreshMutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        fun createDefaultHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                    })
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
        }
    }

    private suspend fun <T> executeRequest(
        requiresAuth: Boolean = false,
        block: suspend (token: String?) -> HttpResponse,
        parser: suspend (HttpResponse) -> T
    ): AppResult<T> {
        return try {
            val token = if (requiresAuth) tokenStorage.getAccessToken() else null
            var response = block(token)

            if (response.status == HttpStatusCode.Unauthorized && requiresAuth) {
                val refreshed = tryRefreshToken()
                if (refreshed) {
                    val newToken = tokenStorage.getAccessToken()
                    response = block(newToken)
                } else {
                    tokenStorage.clear()
                    return AppResult.Error(
                        AppError(
                            message = "Session expired. Please log in again.",
                            statusCode = 401,
                            type = ErrorType.UNAUTHORIZED
                        )
                    )
                }
            }

            if (response.status.isSuccess()) {
                AppResult.Success(parser(response))
            } else {
                val errorBody = try {
                    response.bodyAsText()
                } catch (e: Exception) {
                    ""
                }
                val problem = try {
                    json.decodeFromString<ProblemDetailDto>(errorBody)
                } catch (e: Exception) {
                    null
                }
                val errorType = when (response.status) {
                    HttpStatusCode.BadRequest -> ErrorType.VALIDATION
                    HttpStatusCode.Unauthorized -> ErrorType.UNAUTHORIZED
                    HttpStatusCode.Forbidden -> ErrorType.FORBIDDEN
                    HttpStatusCode.NotFound -> ErrorType.NOT_FOUND
                    HttpStatusCode.Conflict -> ErrorType.CONFLICT
                    HttpStatusCode.InternalServerError -> ErrorType.SERVER
                    else -> ErrorType.UNKNOWN
                }
                AppResult.Error(
                    AppError(
                        message = problem?.detail ?: problem?.title ?: "Request failed with status ${response.status.value}",
                        statusCode = response.status.value,
                        type = errorType,
                        validationErrors = problem?.errors ?: emptyMap()
                    )
                )
            }
        } catch (e: Exception) {
            AppResult.Error(
                AppError(
                    message = e.message ?: "Network communication error",
                    statusCode = null,
                    type = ErrorType.NETWORK
                )
            )
        }
    }

    private suspend fun tryRefreshToken(): Boolean = refreshMutex.withLock {
        val currentRefresh = tokenStorage.getRefreshToken() ?: return false
        return try {
            val response = httpClient.post("${apiConfig.baseUrl}/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequestDto(currentRefresh))
            }
            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                tokenStorage.setAccessToken(authResponse.accessToken)
                tokenStorage.setRefreshToken(authResponse.refreshToken)
                tokenStorage.setUser(authResponse.user.toDomain())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun register(request: RegisterRequestDto): AppResult<AuthResponseDto> {
        return executeRequest(requiresAuth = false, block = {
            httpClient.post("${apiConfig.baseUrl}/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }, parser = { it.body<AuthResponseDto>() })
    }

    suspend fun login(request: LoginRequestDto): AppResult<AuthResponseDto> {
        return executeRequest(requiresAuth = false, block = {
            httpClient.post("${apiConfig.baseUrl}/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }, parser = { it.body<AuthResponseDto>() })
    }

    suspend fun logout(refreshToken: String?): AppResult<Unit> {
        return executeRequest(requiresAuth = true, block = { token ->
            httpClient.post("${apiConfig.baseUrl}/api/v1/auth/logout") {
                contentType(ContentType.Application.Json)
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                setBody(LogoutRequestDto(refreshToken))
            }
        }, parser = { })
    }

    suspend fun getCurrentUser(): AppResult<UserResponseDto> {
        return executeRequest(requiresAuth = true, block = { token ->
            httpClient.get("${apiConfig.baseUrl}/api/v1/me") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
            }
        }, parser = { it.body<UserResponseDto>() })
    }

    suspend fun listPalettes(
        name: String? = null,
        tag: String? = null,
        hexColor: String? = null,
        sort: String = "newest",
        page: Int = 0,
        size: Int = 20
    ): AppResult<PagedResponseDto<PaletteResponseDto>> {
        return executeRequest(requiresAuth = false, block = { token ->
            httpClient.get("${apiConfig.baseUrl}/api/v1/palettes") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                name?.let { parameter("name", it) }
                tag?.let { parameter("tag", it) }
                hexColor?.let { parameter("hexColor", it) }
                parameter("sort", sort)
                parameter("page", page)
                parameter("size", size)
            }
        }, parser = { it.body<PagedResponseDto<PaletteResponseDto>>() })
    }

    suspend fun getPaletteById(id: String): AppResult<PaletteResponseDto> {
        return executeRequest(requiresAuth = false, block = { token ->
            httpClient.get("${apiConfig.baseUrl}/api/v1/palettes/$id") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
            }
        }, parser = { it.body<PaletteResponseDto>() })
    }

    suspend fun getRandomPalette(): AppResult<PaletteResponseDto> {
        return executeRequest(requiresAuth = false, block = { token ->
            httpClient.get("${apiConfig.baseUrl}/api/v1/palettes/random") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
            }
        }, parser = { it.body<PaletteResponseDto>() })
    }

    suspend fun createPalette(request: CreatePaletteRequestDto): AppResult<PaletteResponseDto> {
        return executeRequest(requiresAuth = true, block = { token ->
            httpClient.post("${apiConfig.baseUrl}/api/v1/palettes") {
                contentType(ContentType.Application.Json)
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                setBody(request)
            }
        }, parser = { it.body<PaletteResponseDto>() })
    }

    suspend fun updatePalette(id: String, request: UpdatePaletteRequestDto): AppResult<PaletteResponseDto> {
        return executeRequest(requiresAuth = true, block = { token ->
            httpClient.put("${apiConfig.baseUrl}/api/v1/palettes/$id") {
                contentType(ContentType.Application.Json)
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                setBody(request)
            }
        }, parser = { it.body<PaletteResponseDto>() })
    }

    suspend fun deletePalette(id: String): AppResult<Unit> {
        return executeRequest(requiresAuth = true, block = { token ->
            httpClient.delete("${apiConfig.baseUrl}/api/v1/palettes/$id") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
            }
        }, parser = { })
    }

    suspend fun listMyPalettes(page: Int = 0, size: Int = 20): AppResult<PagedResponseDto<PaletteResponseDto>> {
        return executeRequest(requiresAuth = true, block = { token ->
            httpClient.get("${apiConfig.baseUrl}/api/v1/me/palettes") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("size", size)
            }
        }, parser = { it.body<PagedResponseDto<PaletteResponseDto>>() })
    }

    suspend fun favoritePalette(id: String): AppResult<Unit> {
        return executeRequest(requiresAuth = true, block = { token ->
            httpClient.post("${apiConfig.baseUrl}/api/v1/palettes/$id/favorite") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
            }
        }, parser = { })
    }

    suspend fun unfavoritePalette(id: String): AppResult<Unit> {
        return executeRequest(requiresAuth = true, block = { token ->
            httpClient.delete("${apiConfig.baseUrl}/api/v1/palettes/$id/favorite") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
            }
        }, parser = { })
    }

    suspend fun getUserFavorites(page: Int = 0, size: Int = 20): AppResult<PagedResponseDto<PaletteResponseDto>> {
        return executeRequest(requiresAuth = true, block = { token ->
            httpClient.get("${apiConfig.baseUrl}/api/v1/me/favorites") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("size", size)
            }
        }, parser = { it.body<PagedResponseDto<PaletteResponseDto>>() })
    }
}

fun UserResponseDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    role = Role.fromString(role),
    createdAt = createdAt
)

fun PaletteResponseDto.toDomain(): Palette = Palette(
    id = id,
    name = name,
    status = status,
    likeCount = likeCount,
    colors = colors,
    tags = tags,
    createdBy = createdBy,
    createdAt = createdAt,
    publishedAt = publishedAt,
    likedByMe = likedByMe
)

fun <T, R> PagedResponseDto<T>.toDomain(mapper: (T) -> R): PagedList<R> = PagedList(
    items = items.map(mapper),
    metadata = PageMetadata(
        page = metadata.page,
        size = metadata.size,
        totalElements = metadata.totalElements,
        totalPages = metadata.totalPages,
        hasNext = metadata.hasNext,
        hasPrevious = metadata.hasPrevious
    )
)
