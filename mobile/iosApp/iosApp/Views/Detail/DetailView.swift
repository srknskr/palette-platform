import SwiftUI
import SharedMobile

struct DetailView: View {
    let palette: Palette
    let onAuthRequired: () -> Void

    @State private var copiedHex: String? = nil

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                VStack(spacing: 0) {
                    ForEach(palette.colors, id: \.self) { colorHex in
                        Button(action: {
                            UIPasteboard.general.string = colorHex
                            copiedHex = colorHex
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                copiedHex = nil
                            }
                        }) {
                            HStack {
                                Text(colorHex)
                                    .font(.headline)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                Spacer()
                                Image(systemName: "doc.on.doc")
                                    .foregroundColor(.white.opacity(0.8))
                            }
                            .padding(.horizontal, 20)
                            .frame(maxWidth: .infinity)
                            .frame(height: 80)
                            .background(Color(hex: colorHex))
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Color \(colorHex). Tap to copy.")
                    }
                }
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .shadow(color: Color.black.opacity(0.06), radius: 8, x: 0, y: 4)

                if let copied = copiedHex {
                    Text("Copied \(copied) to clipboard!")
                        .font(.caption)
                        .foregroundColor(.green)
                        .padding(.horizontal, 4)
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text(palette.name)
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.warmTextPrimary)

                    if let desc = palette.paletteDescription, !desc.trimmingCharacters(in: .whitespaces).isEmpty {
                        Text(desc)
                            .font(.body)
                            .foregroundColor(.warmTextSecondary)
                    }

                    Text("\(palette.likeCount) likes • \(palette.status)")
                        .font(.subheadline)
                        .foregroundColor(.warmTextSecondary)
                }

                if !palette.tags.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Tags")
                            .font(.headline)
                            .foregroundColor(.warmTextPrimary)

                        HStack {
                            ForEach(palette.tags, id: \.self) { tag in
                                Text("#\(tag)")
                                    .font(.caption)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 5)
                                    .background(Color.warmSurface)
                                    .clipShape(Capsule())
                            }
                        }
                    }
                }

                ShareLink(
                    item: "Colors: " + palette.colors.joined(separator: ", ")
                ) {
                    Label("Share Palette", systemImage: "square.and.arrow.up")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.warmSurface)
                        .foregroundColor(.warmTextPrimary)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding(16)
        }
        .background(Color.warmBackground)
        .navigationTitle("Details")
        .navigationBarTitleDisplayMode(.inline)
    }
}
