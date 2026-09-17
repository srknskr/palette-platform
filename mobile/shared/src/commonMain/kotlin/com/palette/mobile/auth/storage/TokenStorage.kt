package com.palette.mobile.auth.storage

import com.palette.mobile.auth.model.User

interface TokenStorage {
    suspend fun getAccessToken(): String?
    suspend fun setAccessToken(token: String?)
    suspend fun getRefreshToken(): String?
    suspend fun setRefreshToken(token: String?)
    suspend fun getUser(): User?
    suspend fun setUser(user: User?)
    suspend fun clear()
}
