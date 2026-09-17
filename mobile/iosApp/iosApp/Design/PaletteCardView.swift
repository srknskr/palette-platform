import SwiftUI
import SharedMobile

struct PaletteCardView: View {
    let palette: Palette
    let onPaletteClick: () -> Void
    let onFavoriteClick: () -> Void

    var body: some View {
        Button(action: onPaletteClick) {
            VStack(alignment: .leading, spacing: 0) {
                VStack(spacing: 0) {
                    let heights: [CGFloat] = [70, 45, 35, 30]
                    ForEach(Array(palette.colors.prefix(4).enumerated()), id: \.offset) { index, colorHex in
                        Color(hex: colorHex)
                            .frame(height: heights[index])
                            .accessibilityLabel("Color \(colorHex)")
                    }
                }
                .clipShape(RoundedRectangle(cornerRadius: 16))

                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(palette.name)
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.warmTextPrimary)
                            .lineLimit(1)

                        Text("\(palette.likeCount) likes")
                            .font(.caption2)
                            .foregroundColor(.warmTextSecondary)
                    }

                    Spacer()

                    Button(action: onFavoriteClick) {
                        Image(systemName: palette.likedByMe ? "heart.fill" : "heart")
                            .foregroundColor(palette.likedByMe ? .red : .gray)
                            .font(.system(size: 16))
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(palette.likedByMe ? "Unfavorite palette" : "Favorite palette")
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 8)
                .background(Color.warmSurface)
            }
            .background(Color.warmSurface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .contain)
        .accessibilityLabel("Palette \(palette.name)")
    }
}
