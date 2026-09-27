import SwiftUI
import SharedMobile

struct DetailView: View {
    let palette: Palette
    let onAuthRequired: () -> Void

    @State private var copiedHex: String? = nil

    @State private var showingExportSheet = false
    @State private var showingContrastSheet = false
    @State private var selectedExportFormat: ExportFormat = .css
    @State private var copiedExportMessage: String? = nil

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

                HStack(spacing: 12) {
                    Button(action: { showingContrastSheet = true }) {
                        Label("Contrast", systemImage: "eye")
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.warmSurface)
                            .foregroundColor(.warmTextPrimary)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    Button(action: { showingExportSheet = true }) {
                        Label("Export", systemImage: "curlybraces")
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.warmTextPrimary)
                            .foregroundColor(Color.warmSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
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
        .sheet(isPresented: $showingExportSheet) {
            NavigationStack {
                VStack(alignment: .leading, spacing: 16) {
                    Picker("Format", selection: $selectedExportFormat) {
                        Text("CSS").tag(ExportFormat.css)
                        Text("Tailwind").tag(ExportFormat.tailwind)
                        Text("Compose").tag(ExportFormat.compose)
                        Text("SwiftUI").tag(ExportFormat.swiftUi)
                        Text("JSON").tag(ExportFormat.json)
                    }
                    .pickerStyle(.segmented)

                    let code = PaletteExporter.shared.export(palette: palette, format: selectedExportFormat)

                    ScrollView {
                        Text(code)
                            .font(.system(.subheadline, design: .monospaced))
                            .padding(16)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.warmSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    if let msg = copiedExportMessage {
                        Text(msg)
                            .font(.caption)
                            .foregroundColor(.green)
                    }

                    Button(action: {
                        UIPasteboard.general.string = code
                        copiedExportMessage = "\(selectedExportFormat.displayName) copied to clipboard!"
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            copiedExportMessage = nil
                        }
                    }) {
                        Label("Copy to Clipboard", systemImage: "doc.on.doc")
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.warmTextPrimary)
                            .foregroundColor(Color.warmSurface)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
                .padding(20)
                .background(Color.warmBackground)
                .navigationTitle("Export Palette")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Done") { showingExportSheet = false }
                    }
                }
            }
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showingContrastSheet) {
            NavigationStack {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("WCAG 2.1 Contrast & Accessibility")
                            .font(.headline)
                            .foregroundColor(.warmTextPrimary)

                        Text("Contrast ratio scores for normal text against white and dark backgrounds.")
                            .font(.subheadline)
                            .foregroundColor(.warmTextSecondary)

                        ForEach(Array(palette.colors.enumerated()), id: \.offset) { index, hex in
                            let pair = ColorContrastCalculator.shared.evaluateTextContrastAgainst(backgroundColorHex: hex)
                            let againstWhite = pair.first!
                            let againstDark = pair.second!

                            VStack(spacing: 0) {
                                HStack {
                                    Text("White Text")
                                        .font(.subheadline)
                                        .fontWeight(.semibold)
                                        .foregroundColor(.white)
                                    Spacer()
                                    Text("Dark Text")
                                        .font(.subheadline)
                                        .fontWeight(.semibold)
                                        .foregroundColor(Color(hex: "111827"))
                                }
                                .padding(.horizontal, 16)
                                .frame(height: 50)
                                .background(Color(hex: hex))

                                VStack(spacing: 8) {
                                    HStack {
                                        Text("Color \(index + 1)")
                                            .fontWeight(.bold)
                                        Spacer()
                                        Text(hex)
                                            .font(.system(.caption, design: .monospaced))
                                            .foregroundColor(.warmTextSecondary)
                                    }

                                    HStack {
                                        Text("On White: \(String(format: "%.2f", againstWhite.ratio)):1")
                                            .font(.caption)
                                        Spacer()
                                        Text(againstWhite.levelNormalText.label)
                                            .font(.caption)
                                            .fontWeight(.bold)
                                            .foregroundColor(againstWhite.passesNormalAA ? .green : .red)
                                    }

                                    HStack {
                                        Text("On Dark: \(String(format: "%.2f", againstDark.ratio)):1")
                                            .font(.caption)
                                        Spacer()
                                        Text(againstDark.levelNormalText.label)
                                            .font(.caption)
                                            .fontWeight(.bold)
                                            .foregroundColor(againstDark.passesNormalAA ? .green : .red)
                                    }
                                }
                                .padding(12)
                                .background(Color.warmSurface)
                            }
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                            .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
                        }
                    }
                    .padding(20)
                }
                .background(Color.warmBackground)
                .navigationTitle("Accessibility")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Done") { showingContrastSheet = false }
                    }
                }
            }
            .presentationDetents([.medium, .large])
        }
    }
}
