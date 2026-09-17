import SwiftUI
import SharedMobile

@main
struct PaletteApp: App {
    let apiConfig: ApiConfig
    let tokenStorage: TokenStorage
    let networkClient: NetworkClient
    let authRepository: AuthRepository
    let paletteRepository: PaletteRepository
    let favoriteRepository: FavoriteRepository

    let authViewModel: AuthViewModel
    let discoverViewModel: DiscoverViewModel
    let createViewModel: CreateViewModel
    let collectionViewModel: CollectionViewModel

    init() {
#if targetEnvironment(simulator)
        let baseUrl = ApiConfig.companion.IOS_SIMULATOR_BASE_URL
#else
        let baseUrl = "http://192.168.1.52:8080"
#endif
        let config = ApiConfig(baseUrl: baseUrl, timeoutMillis: 15000)
        let storage = IosKeychainTokenStorage()
        let network = NetworkClient(apiConfig: config, tokenStorage: storage, httpClient: NetworkClient.companion.createDefaultHttpClient())
        let authRepo = AuthRepository(networkClient: network, tokenStorage: storage)
        let paletteRepo = PaletteRepository(networkClient: network)
        let favRepo = FavoriteRepository(networkClient: network, paletteRepository: paletteRepo)

        self.apiConfig = config
        self.tokenStorage = storage
        self.networkClient = network
        self.authRepository = authRepo
        self.paletteRepository = paletteRepo
        self.favoriteRepository = favRepo

        self.authViewModel = AuthViewModel(authRepository: authRepo)
        self.discoverViewModel = DiscoverViewModel(
            getPalettesUseCase: GetPalettesUseCase(paletteRepository: paletteRepo),
            toggleFavoriteUseCase: ToggleFavoriteUseCase(favoriteRepository: favRepo)
        )
        self.createViewModel = CreateViewModel(createPaletteUseCase: CreatePaletteUseCase(paletteRepository: paletteRepo))
        self.collectionViewModel = CollectionViewModel(
            getUserFavoritesUseCase: GetUserFavoritesUseCase(favoriteRepository: favRepo),
            toggleFavoriteUseCase: ToggleFavoriteUseCase(favoriteRepository: favRepo)
        )
    }

    var body: some Scene {
        WindowGroup {
            MainTabView(
                authViewModel: authViewModel,
                discoverViewModel: discoverViewModel,
                createViewModel: createViewModel,
                collectionViewModel: collectionViewModel
            )
        }
    }
}
