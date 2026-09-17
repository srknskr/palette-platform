import Foundation
import SharedMobile

@MainActor
class AuthViewModel: ObservableObject {
    @Published var currentUser: User? = nil
    @Published var isAuthenticated = false
    @Published var isLoading = false
    @Published var errorMessage: String? = nil

    private let authRepository: AuthRepository

    init(authRepository: AuthRepository) {
        self.authRepository = authRepository
    }

    func checkAuth() async {
        do {
            try await authRepository.initialize()
            let state = authRepository.getCurrentAuthState()
            if let authenticated = state as? AuthStateAuthenticated {
                currentUser = authenticated.user
                isAuthenticated = true
            } else {
                currentUser = nil
                isAuthenticated = false
            }
        } catch {
            currentUser = nil
            isAuthenticated = false
        }
    }

    func login(email: String, pass: String) async -> Bool {
        isLoading = true
        errorMessage = nil
        do {
            let result = try await authRepository.login(email: email, password: pass)
            if let success = result as? AppResultSuccess<User>, let user = success.data {
                currentUser = user
                isAuthenticated = true
                isLoading = false
                return true
            } else if let error = result as? AppResultError {
                errorMessage = error.error.message
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
        return false
    }

    func register(email: String, pass: String, displayName: String) async -> Bool {
        isLoading = true
        errorMessage = nil
        do {
            let result = try await authRepository.register(email: email, password: pass, displayName: displayName)
            if let success = result as? AppResultSuccess<User>, let user = success.data {
                currentUser = user
                isAuthenticated = true
                isLoading = false
                return true
            } else if let error = result as? AppResultError {
                errorMessage = error.error.message
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
        return false
    }

    func logout() async {
        _ = try? await authRepository.logout()
        currentUser = nil
        isAuthenticated = false
    }
}
