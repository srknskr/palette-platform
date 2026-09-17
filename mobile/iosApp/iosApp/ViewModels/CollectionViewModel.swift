import Foundation
import SharedMobile

@MainActor
class CollectionViewModel: ObservableObject {
    @Published var palettes: [Palette] = []
    @Published var isLoading = false
    @Published var errorMessage: String? = nil

    private let getUserFavoritesUseCase: GetUserFavoritesUseCase
    private let toggleFavoriteUseCase: ToggleFavoriteUseCase

    init(getUserFavoritesUseCase: GetUserFavoritesUseCase, toggleFavoriteUseCase: ToggleFavoriteUseCase) {
        self.getUserFavoritesUseCase = getUserFavoritesUseCase
        self.toggleFavoriteUseCase = toggleFavoriteUseCase
    }

    func loadFavorites() async {
        if palettes.isEmpty {
            isLoading = true
        }
        errorMessage = nil

        do {
            let result = try await getUserFavoritesUseCase.invoke(page: 0, size: 50)
            if let success = result as? AppResultSuccess<PagedList<Palette>>, let paged = success.data {
                palettes = (paged.items as? [Palette]) ?? []
            } else if let error = result as? AppResultError {
                if palettes.isEmpty {
                    errorMessage = error.error.message
                }
            }
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func toggleFavorite(palette: Palette, onAuthRequired: () -> Void) async {
        let original = palettes
        palettes.removeAll { $0.id == palette.id }

        do {
            let result = try await toggleFavoriteUseCase.invoke(palette: palette)
            if let error = result as? AppResultError {
                if error.error.statusCode?.intValue == 401 {
                    onAuthRequired()
                }
                palettes = original
            }
        } catch {
            palettes = original
        }
    }
}
