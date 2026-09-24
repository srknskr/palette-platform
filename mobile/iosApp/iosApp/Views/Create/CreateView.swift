import SwiftUI
import SharedMobile

struct CreateView: View {
    @ObservedObject var viewModel: CreateViewModel
    let onAuthRequired: () -> Void

    @State private var showingConfirmation = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Create Palette")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.warmTextPrimary)

                    Text("Design a four-color palette")
                        .font(.subheadline)
                        .foregroundColor(.warmTextSecondary)
                }

                VStack(spacing: 0) {
                    ForEach(viewModel.colors, id: \.self) { colorHex in
                        Color(hex: colorHex)
                            .frame(height: 35)
                    }
                }
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)

                VStack(alignment: .leading, spacing: 8) {
                    Text("Palette Name")
                        .font(.headline)
                        .foregroundColor(.warmTextPrimary)

                    TextField("e.g. Nordic Frost", text: $viewModel.name)
                        .padding(12)
                        .background(Color.warmSurface)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("Description (Optional)")
                        .font(.headline)
                        .foregroundColor(.warmTextPrimary)

                    TextField("e.g. A serene winter morning color scheme", text: $viewModel.descriptionText)
                        .padding(12)
                        .background(Color.warmSurface)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("Colors (4 distinct HEX values)")
                        .font(.headline)
                        .foregroundColor(.warmTextPrimary)

                    ForEach(0..<4, id: \.self) { index in
                        HStack(spacing: 12) {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color(hex: viewModel.colors[index]))
                                .frame(width: 44, height: 44)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 8)
                                        .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                                )

                            TextField("#HEX", text: Binding(
                                get: { viewModel.colors[index] },
                                set: { viewModel.updateColor(index: index, hex: $0) }
                            ))
                            .font(.system(.body, design: .monospaced))
                            .padding(10)
                            .background(Color.warmSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 8))

                            ColorPicker("", selection: Binding(
                                get: { Color(hex: viewModel.colors[index]) },
                                set: { newColor in
                                    if let hex = newColor.toHex() {
                                        viewModel.updateColor(index: index, hex: hex)
                                    }
                                }
                            ))
                            .labelsHidden()
                        }
                    }
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("Tags (comma separated)")
                        .font(.headline)
                        .foregroundColor(.warmTextPrimary)

                    TextField("e.g. pastel, cold, minimal", text: $viewModel.tags)
                        .padding(12)
                        .background(Color.warmSurface)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }

                if let error = viewModel.errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                }

                Button(action: {
                    Task {
                        await viewModel.submit(onAuthRequired: onAuthRequired)
                        if viewModel.createdPalette != nil {
                            showingConfirmation = true
                        }
                    }
                }) {
                    HStack {
                        Spacer()
                        if viewModel.isSubmitting {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Text("Publish Palette")
                                .fontWeight(.semibold)
                        }
                        Spacer()
                    }
                    .padding()
                    .background(Color.warmTextPrimary)
                    .foregroundColor(Color.warmSurface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .disabled(viewModel.isSubmitting)
            }
            .padding(16)
        }
        .background(Color.warmBackground)
        .navigationTitle("Create")
        .alert("Palette Published!", isPresented: $showingConfirmation) {
            Button("OK") {
                viewModel.reset()
                viewModel.name = ""
                viewModel.tags = ""
            }
        } message: {
            Text("Your palette has been published successfully.")
        }
    }
}

extension Color {
    func toHex() -> String? {
        let uic = UIColor(self)
        guard let components = uic.cgColor.components, components.count >= 3 else {
            return nil
        }
        let r = Float(components[0])
        let g = Float(components[1])
        let b = Float(components[2])
        return String(format: "#%02lX%02lX%02lX", lroundf(r * 255), lroundf(g * 255), lroundf(b * 255))
    }
}
