import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            #if DEBUG
            .overlay(alignment: .topTrailing) {
                Text("DEBUG")
                    .font(.system(size: 10, weight: .bold))
                    .kerning(1)
                    .foregroundColor(.white)
                    .frame(width: 140)
                    .padding(.vertical, 3)
                    .background(Color(red: 0.83, green: 0.18, blue: 0.18))
                    .rotationEffect(.degrees(45))
                    .offset(x: 40, y: 24)
                    .allowsHitTesting(false)
                    .ignoresSafeArea()
            }
            #endif
    }
}
