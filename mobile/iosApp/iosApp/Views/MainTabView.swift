import SwiftUI
import SharedMobile

struct MainTabView: View {
    @StateObject var authViewModel: AuthViewModel
    @StateObject var discoverViewModel: DiscoverViewModel
    @StateObject var createViewModel: CreateViewModel
    @StateObject var collectionViewModel: CollectionViewModel

    @State private var selectedTab = 0
    @State private var showAuthSheet = false

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack {
                DiscoverView(
                    viewModel: discoverViewModel,
                    onAuthRequired: { showAuthSheet = true }
                )
            }
            .tabItem {
                Label("Discover", systemImage: "sparkles")
            }
            .tag(0)

            NavigationStack {
                CreateView(
                    viewModel: createViewModel,
                    onAuthRequired: { showAuthSheet = true }
                )
            }
            .tabItem {
                Label("Create", systemImage: "plus.circle")
            }
            .tag(1)

            NavigationStack {
                CollectionView(
                    viewModel: collectionViewModel,
                    onAuthRequired: { showAuthSheet = true }
                )
            }
            .tabItem {
                Label("Collection", systemImage: "heart")
            }
            .tag(2)

            NavigationStack {
                ProfileView(
                    authViewModel: authViewModel,
                    onLoginClick: { showAuthSheet = true }
                )
            }
            .tabItem {
                Label("Profile", systemImage: "person.circle")
            }
            .tag(3)
        }
        .tint(.warmTextPrimary)
        .sheet(isPresented: $showAuthSheet) {
            AuthSheetView(authViewModel: authViewModel)
        }
        .task {
            await authViewModel.checkAuth()
        }
    }
}
