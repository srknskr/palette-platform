package com.palette.mobile.auth.storage

import com.palette.mobile.auth.model.User
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

class IosKeychainTokenStorage : TokenStorage {

    private val json = Json { ignoreUnknownKeys = true }
    private val service = "com.palette.mobile"

    override suspend fun getAccessToken(): String? = getString(KEY_ACCESS_TOKEN)

    override suspend fun setAccessToken(token: String?) {
        if (token != null) saveString(KEY_ACCESS_TOKEN, token) else deleteKey(KEY_ACCESS_TOKEN)
    }

    override suspend fun getRefreshToken(): String? = getString(KEY_REFRESH_TOKEN)

    override suspend fun setRefreshToken(token: String?) {
        if (token != null) saveString(KEY_REFRESH_TOKEN, token) else deleteKey(KEY_REFRESH_TOKEN)
    }

    override suspend fun getUser(): User? {
        val userJson = getString(KEY_USER) ?: return null
        return try {
            json.decodeFromString<User>(userJson)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setUser(user: User?) {
        if (user != null) {
            saveString(KEY_USER, json.encodeToString(user))
        } else {
            deleteKey(KEY_USER)
        }
    }

    override suspend fun clear() {
        deleteKey(KEY_ACCESS_TOKEN)
        deleteKey(KEY_REFRESH_TOKEN)
        deleteKey(KEY_USER)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun saveString(key: String, value: String) {
        val data = NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        deleteKey(key)

        val query = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        )
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, CFBridgingRetain(NSString.create(string = service)))
        CFDictionarySetValue(query, kSecAttrAccount, CFBridgingRetain(NSString.create(string = key)))
        CFDictionarySetValue(query, kSecValueData, CFBridgingRetain(data))

        SecItemAdd(query, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getString(key: String): String? {
        val query = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        )
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, CFBridgingRetain(NSString.create(string = service)))
        CFDictionarySetValue(query, kSecAttrAccount, CFBridgingRetain(NSString.create(string = key)))
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)

        memScoped {
            val result = alloc<kotlinx.cinterop.COpaquePointerVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            if (status == errSecSuccess) {
                val data = CFBridgingRelease(result.value) as? NSData ?: return null
                return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
            }
        }
        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deleteKey(key: String) {
        val query = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        )
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, CFBridgingRetain(NSString.create(string = service)))
        CFDictionarySetValue(query, kSecAttrAccount, CFBridgingRetain(NSString.create(string = key)))

        SecItemDelete(query)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "user_data"
    }
}
