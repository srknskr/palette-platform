package com.palette.mobile.auth.storage

import com.palette.mobile.auth.model.User
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryTokenStorage : TokenStorage {
    private val mutex = Mutex()
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var user: User? = null

    override suspend fun getAccessToken(): String? = mutex.withLock { accessToken }

    override suspend fun setAccessToken(token: String?) {
        mutex.withLock { accessToken = token }
    }

    override suspend fun getRefreshToken(): String? = mutex.withLock { refreshToken }

    override suspend fun setRefreshToken(token: String?) {
        mutex.withLock { refreshToken = token }
    }

    override suspend fun getUser(): User? = mutex.withLock { user }

    override suspend fun setUser(user: User?) {
        mutex.withLock { this.user = user }
    }

    override suspend fun clear() {
        mutex.withLock {
            accessToken = null
            refreshToken = null
            user = null
        }
    }
}
