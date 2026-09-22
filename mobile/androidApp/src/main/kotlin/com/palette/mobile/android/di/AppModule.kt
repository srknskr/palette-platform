package com.palette.mobile.android.di

import android.content.Context
import com.palette.mobile.auth.repository.AuthRepository
import com.palette.mobile.auth.storage.AndroidEncryptedTokenStorage
import com.palette.mobile.auth.storage.TokenStorage
import com.palette.mobile.auth.usecase.GetAuthStateUseCase
import com.palette.mobile.auth.usecase.InitializeAuthUseCase
import com.palette.mobile.auth.usecase.LoginUseCase
import com.palette.mobile.auth.usecase.LogoutUseCase
import com.palette.mobile.auth.usecase.RegisterUseCase
import com.palette.mobile.favorite.repository.FavoriteRepository
import com.palette.mobile.favorite.usecase.GetUserFavoritesUseCase
import com.palette.mobile.favorite.usecase.ObserveFavoritesUseCase
import com.palette.mobile.favorite.usecase.ToggleFavoriteUseCase
import com.palette.mobile.network.ApiConfig
import com.palette.mobile.network.NetworkClient
import com.palette.mobile.palette.repository.PaletteRepository
import com.palette.mobile.palette.usecase.CreatePaletteUseCase
import com.palette.mobile.palette.usecase.DeletePaletteUseCase
import com.palette.mobile.palette.usecase.GetMyPalettesUseCase
import com.palette.mobile.palette.usecase.GetPaletteDetailUseCase
import com.palette.mobile.palette.usecase.GetPalettesUseCase
import com.palette.mobile.palette.usecase.GetRandomPaletteUseCase
import com.palette.mobile.palette.usecase.GetPublishedPaletteCountUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiConfig(): ApiConfig {
        return ApiConfig(baseUrl = ApiConfig.ANDROID_EMULATOR_BASE_URL)
    }

    @Provides
    @Singleton
    fun provideTokenStorage(@ApplicationContext context: Context): TokenStorage {
        return AndroidEncryptedTokenStorage(context)
    }

    @Provides
    @Singleton
    fun provideNetworkClient(apiConfig: ApiConfig, tokenStorage: TokenStorage): NetworkClient {
        return NetworkClient(apiConfig = apiConfig, tokenStorage = tokenStorage)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(networkClient: NetworkClient, tokenStorage: TokenStorage): AuthRepository {
        return AuthRepository(networkClient = networkClient, tokenStorage = tokenStorage)
    }

    @Provides
    @Singleton
    fun providePaletteRepository(networkClient: NetworkClient): PaletteRepository {
        return PaletteRepository(networkClient = networkClient)
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(
        networkClient: NetworkClient,
        paletteRepository: PaletteRepository
    ): FavoriteRepository {
        return FavoriteRepository(
            networkClient = networkClient,
            paletteRepository = paletteRepository
        )
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase = LoginUseCase(authRepository)

    @Provides
    @Singleton
    fun provideRegisterUseCase(authRepository: AuthRepository): RegisterUseCase = RegisterUseCase(authRepository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(authRepository: AuthRepository): LogoutUseCase = LogoutUseCase(authRepository)

    @Provides
    @Singleton
    fun provideGetAuthStateUseCase(authRepository: AuthRepository): GetAuthStateUseCase = GetAuthStateUseCase(authRepository)

    @Provides
    @Singleton
    fun provideInitializeAuthUseCase(authRepository: AuthRepository): InitializeAuthUseCase = InitializeAuthUseCase(authRepository)

    @Provides
    @Singleton
    fun provideGetPalettesUseCase(paletteRepository: PaletteRepository): GetPalettesUseCase = GetPalettesUseCase(paletteRepository)

    @Provides
    @Singleton
    fun provideGetPaletteDetailUseCase(paletteRepository: PaletteRepository): GetPaletteDetailUseCase = GetPaletteDetailUseCase(paletteRepository)

    @Provides
    @Singleton
    fun provideGetRandomPaletteUseCase(paletteRepository: PaletteRepository): GetRandomPaletteUseCase = GetRandomPaletteUseCase(paletteRepository)

    @Provides
    @Singleton
    fun provideCreatePaletteUseCase(paletteRepository: PaletteRepository): CreatePaletteUseCase = CreatePaletteUseCase(paletteRepository)

    @Provides
    @Singleton
    fun provideDeletePaletteUseCase(paletteRepository: PaletteRepository): DeletePaletteUseCase = DeletePaletteUseCase(paletteRepository)

    @Provides
    @Singleton
    fun provideGetMyPalettesUseCase(paletteRepository: PaletteRepository): GetMyPalettesUseCase = GetMyPalettesUseCase(paletteRepository)

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(favoriteRepository: FavoriteRepository): ToggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository)

    @Provides
    @Singleton
    fun provideGetUserFavoritesUseCase(favoriteRepository: FavoriteRepository): GetUserFavoritesUseCase = GetUserFavoritesUseCase(favoriteRepository)

    @Provides
    @Singleton
    fun provideObserveFavoritesUseCase(favoriteRepository: FavoriteRepository): ObserveFavoritesUseCase = ObserveFavoritesUseCase(favoriteRepository)

    @Provides
    @Singleton
    fun provideGetPublishedPaletteCountUseCase(paletteRepository: PaletteRepository): GetPublishedPaletteCountUseCase {
        return GetPublishedPaletteCountUseCase(paletteRepository)
    }
}
