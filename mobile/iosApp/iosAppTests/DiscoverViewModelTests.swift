import XCTest
import SharedMobile

final class DiscoverViewModelTests: XCTestCase {

    func testSortDefaultsToNewest() {
        let storage = InMemoryTokenStorage()
        let config = ApiConfig(baseUrl: "http://localhost:8080", timeoutMillis: 5000)
        let network = NetworkClient(apiConfig: config, tokenStorage: storage, httpClient: NetworkClient.Companion().createDefaultHttpClient())
        let paletteRepo = PaletteRepository(networkClient: network)
        let favRepo = FavoriteRepository(networkClient: network, paletteRepository: paletteRepo)

        let vm = DiscoverViewModel(
            getPalettesUseCase: GetPalettesUseCase(paletteRepository: paletteRepo),
            toggleFavoriteUseCase: ToggleFavoriteUseCase(favoriteRepository: favRepo)
        )

        XCTAssertEqual(vm.selectedSort, .newest)
        XCTAssertTrue(vm.palettes.isEmpty)
        XCTAssertFalse(vm.isLoading)
    }
}
