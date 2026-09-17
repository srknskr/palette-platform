package com.palette.mobile.auth.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.palette.mobile.auth.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidEncryptedTokenStorage(context: Context) : TokenStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "palette_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun setAccessToken(token: String?) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                if (token != null) {
                    putString(KEY_ACCESS_TOKEN, token)
                } else {
                    remove(KEY_ACCESS_TOKEN)
                }
                apply()
            }
        }
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun setRefreshToken(token: String?) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                if (token != null) {
                    putString(KEY_REFRESH_TOKEN, token)
                } else {
                    remove(KEY_REFRESH_TOKEN)
                }
                apply()
            }
        }
    }

    override suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        val userJson = sharedPreferences.getString(KEY_USER, null) ?: return@withContext null
        try {
            json.decodeFromString<User>(userJson)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setUser(user: User?) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                if (user != null) {
                    putString(KEY_USER, json.encodeToString(user))
                } else {
                    remove(KEY_USER)
                }
                apply()
            }
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().clear().apply()
        }
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "user_data"
    }
}
