import Foundation
import SharedMobile

@MainActor
class DiscoverViewModel: ObservableObject {
    @Published var palettes: [Palette] = []
    @Published var isLoading = false
    @Published var errorMessage: String? = nil
    @Published var searchQuery = ""
    @Published var selectedSort: PaletteSort = .newest
    @Published var hasNext = false

    private let getPalettesUseCase: GetPalettesUseCase
    private let toggleFavoriteUseCase: ToggleFavoriteUseCase
    private var currentPage = 0

    init(getPalettesUseCase: GetPalettesUseCase, toggleFavoriteUseCase: ToggleFavoriteUseCase) {
        self.getPalettesUseCase = getPalettesUseCase
        self.toggleFavoriteUseCase = toggleFavoriteUseCase
    }

    func loadPalettes(isRefresh: Bool = false) async {
        if isRefresh {
            currentPage = 0
        }
        if currentPage == 0 && palettes.isEmpty {
            isLoading = true
        }
        errorMessage = nil

        let query = searchQuery.trimmingCharacters(in: .whitespaces).isEmpty ? nil : searchQuery.trimmingCharacters(in: .whitespaces)
        let filter = PaletteFilter(query: query, tag: nil, hexColor: nil, sort: selectedSort)

        do {
            let result = try await getPalettesUseCase.invoke(filter: filter, page: Int32(currentPage), size: 20)
            if let success = result as? AppResultSuccess<PagedList<Palette>>, let paged = success.data {
                let items = (paged.items as? [Palette]) ?? []
                if currentPage == 0 {
                    palettes = items
                } else {
                    palettes.append(contentsOf: items)
                }
                hasNext = paged.metadata.hasNext
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

    func loadNextPage() async {
        guard hasNext && !isLoading else { return }
        currentPage += 1
        await loadPalettes(isRefresh: false)
    }

    func toggleFavorite(palette: Palette, onAuthRequired: () -> Void) async {
        do {
            let result = try await toggleFavoriteUseCase.invoke(palette: palette)
            if let success = result as? AppResultSuccess<KotlinBoolean>, let likedBool = success.data?.boolValue {
                if let index = palettes.firstIndex(where: { $0.id == palette.id }) {
                    let current = palettes[index]
                    let newCount = likedBool ? current.likeCount + 1 : max(0, current.likeCount - 1)
                    palettes[index] = current.doCopy(
                        id: current.id,
                        name: current.name,
                        status: current.status,
                        likeCount: newCount,
                        colors: current.colors,
                        tags: current.tags,
                        createdBy: current.createdBy,
                        createdAt: current.createdAt,
                        publishedAt: current.publishedAt,
                        likedByMe: likedBool
                    )
                }
            } else if let error = result as? AppResultError {
                if error.error.statusCode?.intValue == 401 {
                    onAuthRequired()
                }
            }
        } catch {
        }
    }
}
