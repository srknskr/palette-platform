import XCTest
import SharedMobile

final class ColorValidatorTests: XCTestCase {

    func testValidHexReturnsTrue() {
        XCTAssertTrue(ColorValidator.shared.isValidHex(hex: "#2E3440"))
        XCTAssertTrue(ColorValidator.shared.isValidHex(hex: "#FFFFFF"))
        XCTAssertTrue(ColorValidator.shared.isValidHex(hex: "#000000"))
    }

    func testInvalidHexReturnsFalse() {
        XCTAssertFalse(ColorValidator.shared.isValidHex(hex: "2E3440"))
        XCTAssertFalse(ColorValidator.shared.isValidHex(hex: "#GGG"))
        XCTAssertFalse(ColorValidator.shared.isValidHex(hex: ""))
    }

    func testNormalizeHex() {
        XCTAssertEqual(ColorValidator.shared.normalizeHex(hex: "ffffff"), "#FFFFFF")
        XCTAssertEqual(ColorValidator.shared.normalizeHex(hex: "#2e3440"), "#2E3440")
    }

    func testValidateFourColors() {
        let colors = ["#2E3440", "#4C566A", "#D8DEE9", "#ECEFF4"]
        let res = ColorValidator.shared.validatePaletteColors(colors: colors)
        XCTAssertTrue(res is ValidationResultValid)
    }

    func testValidateDuplicateColorsFails() {
        let colors = ["#2E3440", "#4C566A", "#2E3440", "#ECEFF4"]
        let res = ColorValidator.shared.validatePaletteColors(colors: colors)
        XCTAssertTrue(res is ValidationResultInvalid)
    }
}
