import SwiftUI
import SharedMobile

struct CollectionView: View {
    @ObservedObject var viewModel: CollectionViewModel
    let onAuthRequired: () -> Void

    let columns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]

    var body: some View {
        VStack {
            if viewModel.isLoading && viewModel.palettes.isEmpty {
                Spacer()
                ProgressView()
                Spacer()
            } else if viewModel.palettes.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "heart.slash")
                        .font(.system(size: 48))
                        .foregroundColor(.gray)
                    Text("No saved palettes yet.")
                        .foregroundColor(.gray)
                    Text("Browse Discover to favorite palettes you love.")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
                .padding()
                Spacer()
            } else {
                ScrollView {
                    LazyVGrid(columns: columns, spacing: 12) {
                        ForEach(viewModel.palettes, id: \.id) { palette in
                            NavigationLink(destination: DetailView(palette: palette, onAuthRequired: onAuthRequired)) {
                                PaletteCardView(
                                    palette: palette,
                                    onPaletteClick: { },
                                    onFavoriteClick: {
                                        Task {
                                            await viewModel.toggleFavorite(palette: palette, onAuthRequired: onAuthRequired)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    .padding(16)
                }
                .refreshable {
                    await viewModel.loadFavorites()
                }
            }
        }
        .background(Color.warmBackground)
        .navigationTitle("Collection")
        .task {
            await viewModel.loadFavorites()
        }
    }
}
