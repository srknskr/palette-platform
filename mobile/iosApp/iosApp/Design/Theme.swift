import SwiftUI

extension Color {
    static let warmBackground = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(red: 0.07, green: 0.07, blue: 0.07, alpha: 1.0) : UIColor(red: 0.98, green: 0.97, blue: 0.96, alpha: 1.0)
    })
    
    static let warmSurface = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(red: 0.12, green: 0.12, blue: 0.12, alpha: 1.0) : UIColor.white
    })
    
    static let warmTextPrimary = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(red: 0.92, green: 0.94, blue: 0.96, alpha: 1.0) : UIColor(red: 0.12, green: 0.12, blue: 0.12, alpha: 1.0)
    })
    
    static let warmTextSecondary = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(red: 0.63, green: 0.63, blue: 0.63, alpha: 1.0) : UIColor(red: 0.46, green: 0.46, blue: 0.46, alpha: 1.0)
    })
    
    init(hex: String) {
        let cleanHex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: cleanHex).scanHexInt64(&int)
        let r, g, b: UInt64
        switch cleanHex.count {
        case 6:
            (r, g, b) = ((int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        default:
            (r, g, b) = (128, 128, 128)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: 1
        )
    }
}
