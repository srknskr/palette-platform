import Foundation
import SharedMobile

@MainActor
class CreateViewModel: ObservableObject {
    @Published var name = ""
    @Published var colors = ["#2E3440", "#4C566A", "#D8DEE9", "#ECEFF4"]
    @Published var tags = ""
    @Published var isSubmitting = false
    @Published var errorMessage: String? = nil
    @Published var createdPalette: Palette? = nil

    private let createPaletteUseCase: CreatePaletteUseCase

    init(createPaletteUseCase: CreatePaletteUseCase) {
        self.createPaletteUseCase = createPaletteUseCase
    }

    func updateColor(index: Int, hex: String) {
        guard index >= 0 && index < 4 else { return }
        colors[index] = hex
    }

    func submit(onAuthRequired: () -> Void) async {
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        guard trimmedName.count >= 2 && trimmedName.count <= 80 else {
            errorMessage = "Name must be between 2 and 80 characters"
            return
        }

        let validation = ColorValidator.shared.validatePaletteColors(colors: colors)
        if let invalid = validation as? ValidationResultInvalid {
            errorMessage = invalid.reason
            return
        }

        guard let valid = validation as? ValidationResultValid else { return }

        let tagList = tags.split(separator: ",")
            .map { String($0).trimmingCharacters(in: .whitespaces).lowercased() }
            .filter { !$0.isEmpty }

        isSubmitting = true
        errorMessage = nil

        do {
            let result = try await createPaletteUseCase.invoke(
                name: trimmedName,
                colors: valid.normalizedColors,
                tags: tagList,
                publish: true
            )

            if let success = result as? AppResultSuccess<Palette> {
                createdPalette = success.data
            } else if let error = result as? AppResultError {
                if error.error.statusCode?.intValue == 401 {
                    onAuthRequired()
                }
                errorMessage = error.error.message
            }
        } catch {
            errorMessage = error.localizedDescription
        }

        isSubmitting = false
    }

    func reset() {
        createdPalette = nil
        errorMessage = nil
    }
}
