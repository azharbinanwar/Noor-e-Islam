import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // AppEdition.quran — Kotlin/Native lowercases the first letter of enum entries for Swift.
        // If Xcode's autocomplete disagrees (e.g. AppEdition.QURAN), use whatever it suggests.
        DIKt.startKoinForIos(edition: AppEdition.quran)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
