import SwiftUI
import SharedMobile

struct ProfileView: View {
    @ObservedObject var authViewModel: AuthViewModel
    let onLoginClick: () -> Void

    var body: some View {
        VStack {
            if authViewModel.isAuthenticated, let user = authViewModel.currentUser {
                VStack(spacing: 20) {
                    HStack(spacing: 16) {
                        ZStack {
                            Circle()
                                .fill(Color.warmTextPrimary)
                                .frame(width: 60, height: 60)
                            Text(String(user.displayName.prefix(1)).uppercased())
                                .font(.title)
                                .fontWeight(.bold)
                                .foregroundColor(Color.warmSurface)
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text(user.displayName)
                                .font(.headline)
                                .foregroundColor(.warmTextPrimary)
                            Text(user.email)
                                .font(.subheadline)
                                .foregroundColor(.warmTextSecondary)
                        }

                        Spacer()
                    }
                    .padding()
                    .background(Color.warmSurface)
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    Button(action: {
                        Task { await authViewModel.logout() }
                    }) {
                        HStack {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                            Text("Log Out")
                        }
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.warmSurface)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    Spacer()
                }
                .padding(16)
            } else {
                VStack(spacing: 16) {
                    Spacer()
                    Image(systemName: "person.circle")
                        .font(.system(size: 80))
                        .foregroundColor(.gray)

                    Text("Sign in to your account")
                        .font(.title3)
                        .fontWeight(.bold)

                    Text("Log in to publish palettes, keep a collection of favorites, and more.")
                        .font(.body)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)

                    Button(action: onLoginClick) {
                        Text("Sign In or Register")
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.warmTextPrimary)
                            .foregroundColor(Color.warmSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .padding(.horizontal, 32)
                    .padding(.top, 8)

                    Spacer()
                }
            }
        }
        .background(Color.warmBackground)
        .navigationTitle("Profile")
    }
}
