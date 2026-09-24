import XCTest
import SharedMobile
@testable import iosApp

final class DiscoverViewModelTests: XCTestCase {

    @MainActor
    func testSortDefaultsToNewest() {
        let storage = InMemoryTokenStorage()
        let config = ApiConfig(baseUrl: "http://localhost:8080", timeoutMillis: 5000)
        let network = NetworkClient(apiConfig: config, tokenStorage: storage, httpClient: NetworkClient.Companion().createDefaultHttpClient())
        let paletteRepo = PaletteRepository(networkClient: network)
        let favRepo = FavoriteRepository(networkClient: network, paletteRepository: paletteRepo)

        let vm = DiscoverViewModel(
            getPalettesUseCase: GetPalettesUseCase(paletteRepository: paletteRepo),
            toggleFavoriteUseCase: ToggleFavoriteUseCase(favoriteRepository: favRepo),
            getPublishedPaletteCountUseCase: GetPublishedPaletteCountUseCase(paletteRepository: paletteRepo)
        )

        XCTAssertEqual(vm.selectedSort, .newest)
        XCTAssertTrue(vm.palettes.isEmpty)
        XCTAssertFalse(vm.isLoading)
        XCTAssertNil(vm.paletteCount)
    }
}
