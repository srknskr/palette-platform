import SwiftUI
import SharedMobile

struct DiscoverView: View {
    @ObservedObject var viewModel: DiscoverViewModel
    let onAuthRequired: () -> Void

    let columns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.gray)
                TextField("Search palettes, #hex, or tag", text: $viewModel.searchQuery)
                    .textFieldStyle(.plain)
                    .onSubmit {
                        Task { await viewModel.loadPalettes(isRefresh: true) }
                    }
                if !viewModel.searchQuery.isEmpty {
                    Button(action: {
                        viewModel.searchQuery = ""
                        Task { await viewModel.loadPalettes(isRefresh: true) }
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.gray)
                    }
                }
            }
            .padding(10)
            .background(Color.warmSurface)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .padding(.horizontal, 16)

            HStack(spacing: 8) {
                FilterButton(title: "New", isSelected: viewModel.selectedSort == .newest) {
                    viewModel.selectedSort = .newest
                    Task { await viewModel.loadPalettes(isRefresh: true) }
                }
                FilterButton(title: "Popular", isSelected: viewModel.selectedSort == .popular) {
                    viewModel.selectedSort = .popular
                    Task { await viewModel.loadPalettes(isRefresh: true) }
                }
                FilterButton(title: "Random", isSelected: viewModel.selectedSort == .random) {
                    viewModel.selectedSort = .random
                    Task { await viewModel.loadPalettes(isRefresh: true) }
                }
                Spacer()
            }
            .padding(.horizontal, 16)

            if viewModel.isLoading && viewModel.palettes.isEmpty {
                Spacer()
                ProgressView()
                Spacer()
            } else if let error = viewModel.errorMessage, viewModel.palettes.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 44))
                        .foregroundColor(.red)
                    Text(error)
                        .font(.body)
                        .multilineTextAlignment(.center)
                    Button("Retry") {
                        Task { await viewModel.loadPalettes(isRefresh: true) }
                    }
                }
                .padding()
                Spacer()
            } else if viewModel.palettes.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "paintpalette")
                        .font(.system(size: 48))
                        .foregroundColor(.gray)
                    Text("No palettes found.")
                        .foregroundColor(.gray)
                }
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
                    await viewModel.loadPalettes(isRefresh: true)
                }
            }
        }
        .background(Color.warmBackground)
        .navigationTitle("Palette")
        .task {
            if viewModel.palettes.isEmpty {
                await viewModel.loadPalettes(isRefresh: true)
            }
        }
    }
}

struct FilterButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .fontWeight(isSelected ? .semibold : .regular)
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(isSelected ? Color.warmTextPrimary : Color.warmSurface)
                .foregroundColor(isSelected ? Color.warmSurface : Color.warmTextPrimary)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}
