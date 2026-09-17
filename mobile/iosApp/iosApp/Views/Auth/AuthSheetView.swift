import SwiftUI
import SharedMobile

struct AuthSheetView: View {
    @ObservedObject var authViewModel: AuthViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var isRegister = false
    @State private var email = ""
    @State private var password = ""
    @State private var displayName = ""

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                VStack(spacing: 6) {
                    Text(isRegister ? "Join Palette" : "Welcome Back")
                        .font(.title2)
                        .fontWeight(.bold)
                    Text(isRegister ? "Create an account to publish and favorite" : "Sign in to your Palette account")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }
                .padding(.top, 24)

                VStack(spacing: 12) {
                    if isRegister {
                        TextField("Display Name", text: $displayName)
                            .padding(12)
                            .background(Color.warmSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                    }

                    TextField("Email", text: $email)
                        .autocapitalization(.none)
                        .keyboardType(.emailAddress)
                        .padding(12)
                        .background(Color.warmSurface)
                        .clipShape(RoundedRectangle(cornerRadius: 10))

                    SecureField("Password", text: $password)
                        .padding(12)
                        .background(Color.warmSurface)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .padding(.horizontal, 20)

                if let error = authViewModel.errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                        .padding(.horizontal, 20)
                }

                Button(action: {
                    Task {
                        let success: Bool
                        if isRegister {
                            success = await authViewModel.register(email: email, pass: password, displayName: displayName)
                        } else {
                            success = await authViewModel.login(email: email, pass: password)
                        }
                        if success {
                            dismiss()
                        }
                    }
                }) {
                    HStack {
                        Spacer()
                        if authViewModel.isLoading {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Text(isRegister ? "Create Account" : "Sign In")
                                .fontWeight(.semibold)
                        }
                        Spacer()
                    }
                    .padding()
                    .background(Color.warmTextPrimary)
                    .foregroundColor(Color.warmSurface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding(.horizontal, 20)
                .disabled(authViewModel.isLoading)

                Button(action: {
                    isRegister.toggle()
                    authViewModel.errorMessage = nil
                }) {
                    Text(isRegister ? "Already have an account? Sign In" : "Don't have an account? Create one")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }

                Spacer()
            }
            .background(Color.warmBackground)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
        }
    }
}
