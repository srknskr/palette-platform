package com.palette.mobile.network

data class ApiConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
    val timeoutMillis: Long = 15_000L
) {
    companion object {
        const val DEFAULT_BASE_URL = "http://localhost:8080"
        const val ANDROID_EMULATOR_BASE_URL = "http://10.0.2.2:8080"
        const val IOS_SIMULATOR_BASE_URL = "http://localhost:8080"
    }
}
