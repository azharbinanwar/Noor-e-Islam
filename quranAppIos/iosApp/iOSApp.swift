import SwiftUI
import Shared
import UserNotifications
import BackgroundTasks

private let notifRefreshId = "dev.miqat.notif.refresh"

// Best-effort background top-up. iOS decides when (if ever) it runs, so it's a supplement to the
// foreground refill, not a guarantee.
private func scheduleNotifRefresh() {
    let req = BGAppRefreshTaskRequest(identifier: notifRefreshId)
    req.earliestBeginDate = Date(timeIntervalSinceNow: 6 * 60 * 60) // ~6h
    try? BGTaskScheduler.shared.submit(req)
}

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @State private var showSplash = true

    init() {
        // AppEdition.quran — Kotlin/Native lowercases the first letter of enum entries for Swift.
        // If Xcode's autocomplete disagrees (e.g. AppEdition.QURAN), use whatever it suggests.
        DIKt.startKoinForIos(edition: AppEdition.quran)
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
        NotificationScheduler.shared.start() // build Surah Al-Mulk/Al-Kahf reminders + re-arm on any change
        BGTaskScheduler.shared.register(forTaskWithIdentifier: notifRefreshId, using: nil) { task in
            NotificationScheduler.shared.rebuildAsync()
            scheduleNotifRefresh()               // chain the next one
            task.setTaskCompleted(success: true)
        }
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                ContentView()
                // Stage 2 of the splash: the OS launch screen is the same solid green (UILaunchScreen),
                // this overlay continues it seamlessly and plays the logo animation, then fades away.
                if showSplash {
                    SplashView {
                        withAnimation(.easeOut(duration: 0.3)) { showSplash = false }
                    }
                    .transition(.opacity)
                    .zIndex(1)
                }
            }
            .onChange(of: scenePhase) { phase in
                if phase == .active { NotificationScheduler.shared.rebuildAsync() } // top up on every open
                if phase == .background { scheduleNotifRefresh() }
            }
        }
    }
}

// ───────────────────────── splash: "Bloom Upward" ─────────────────────────
// Wings light up bottom-to-top, the swoosh completes the open Quran, then the
// mosque pops in above it. Timings mirror the Android AVD (avd_splash.xml).

private struct SplashView: View {
    let onFinished: () -> Void
    @State private var wingL2 = false
    @State private var wingR2 = false
    @State private var wingL1 = false
    @State private var wingR1 = false
    @State private var swoosh = false
    @State private var mosque = false

    var body: some View {
        ZStack {
            Color(red: 0x1E / 255.0, green: 0x7D / 255.0, blue: 0x55 / 255.0)
                .ignoresSafeArea()
            ZStack {
                LogoShape(.mosque).fill(.white)
                    .opacity(mosque ? 1 : 0)
                    .scaleEffect(mosque ? 1 : 0.6, anchor: UnitPoint(x: 0.5, y: 0.42))
                LogoShape(.swoosh).fill(.white).opacity(swoosh ? 1 : 0)
                LogoShape(.wingR1).fill(.white).opacity(wingR1 ? 1 : 0)
                LogoShape(.wingL1).fill(.white).opacity(wingL1 ? 1 : 0)
                LogoShape(.wingR2).fill(.white).opacity(wingR2 ? 1 : 0)
                LogoShape(.wingL2).fill(.white).opacity(wingL2 ? 1 : 0)
            }
            .frame(width: 150, height: 171) // logo aspect 1845:2104
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.25)) { wingL2 = true }
            withAnimation(.easeOut(duration: 0.25).delay(0.05)) { wingR2 = true }
            withAnimation(.easeOut(duration: 0.25).delay(0.14)) { wingL1 = true }
            withAnimation(.easeOut(duration: 0.25).delay(0.19)) { wingR1 = true }
            withAnimation(.easeOut(duration: 0.30).delay(0.28)) { swoosh = true }
            withAnimation(.spring(response: 0.45, dampingFraction: 0.55).delay(0.45)) { mosque = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { onFinished() }
        }
    }
}

// The logo's six SVG paths, parsed once and scaled to fit whatever frame the shape gets.
private struct LogoShape: Shape {
    enum Part { case mosque, swoosh, wingR1, wingL1, wingR2, wingL2 }
    let part: Part
    init(_ part: Part) { self.part = part }

    // source artwork viewBox
    private static let vbW: CGFloat = 1845
    private static let vbH: CGFloat = 2104

    func path(in rect: CGRect) -> Path {
        let base: Path
        switch part {
        case .mosque: base = Self.mosque
        case .swoosh: base = Self.swoosh
        case .wingR1: base = Self.wingR1
        case .wingL1: base = Self.wingL1
        case .wingR2: base = Self.wingR2
        case .wingL2: base = Self.wingL2
        }
        let scale = min(rect.width / Self.vbW, rect.height / Self.vbH)
        let t = CGAffineTransform(translationX: rect.midX - Self.vbW * scale / 2,
                                  y: rect.midY - Self.vbH * scale / 2)
            .scaledBy(x: scale, y: scale)
        return base.applying(t)
    }

    static let mosque = parse("M910.462 0C842.772 23.2908 794.13 87.5201 794.13 163.114C794.134 258.363 871.344 335.572 966.588 335.572C1042.19 335.572 1106.42 286.93 1129.7 219.236C1124.42 323.508 1042.63 407.573 939.355 416.467V482.929C963.64 489.983 979.193 502.868 990.776 519.163C1003.95 537.689 1012.09 560.932 1022.34 583.518C1032.73 606.398 1045.6 629.57 1068.51 649.847C1079.34 659.426 1092.49 668.42 1108.79 676.43C1444.24 747.901 1692.73 1006.64 1692.73 1314.9C1692.73 1332.82 1691.88 1350.58 1690.23 1368.14C1315.57 1370.51 1027.63 1613.45 921.928 1749.83C815.991 1613.14 527.877 1369.4 152.166 1368.12C150.517 1350.57 149.671 1332.81 149.671 1314.9C149.673 1002.04 405.65 740.168 748.711 673.342C762.271 666.134 773.507 658.219 782.969 649.847C805.888 629.569 818.762 606.398 829.143 583.518C839.393 560.932 847.529 537.688 860.7 519.163C872.287 502.868 887.841 489.983 912.126 482.929V417.051C801.042 412.3 712.439 320.757 712.438 208.505C712.438 96.8062 800.159 5.59095 910.462 0Z")
    static let swoosh = parse("M1837.83 1379.71C1392.09 1306.7 1041.12 1596.03 921.927 1749.83C802.732 1596.03 452.908 1306.7 7.17276 1379.71C0.242784 1388.16 -7.79597 1407.09 15.4887 1415.2C44.5945 1425.34 572.657 1354.36 921.927 1775.18C1271.2 1354.36 1800.41 1425.34 1829.51 1415.2C1852.8 1407.09 1844.76 1388.16 1837.83 1379.71Z")
    static let wingR1 = parse("M1827.88 1503.81C1342.23 1430.8 1040.64 1726.9 950.55 1884.07C933.918 1916.52 954.708 1907.73 967.182 1899.28C1253.25 1643.75 1640.77 1603.52 1798.78 1615.35C1858.65 1582.91 1843.13 1527.47 1827.88 1503.81Z")
    static let wingL1 = parse("M22.1722 1503.81C507.824 1430.8 809.416 1726.9 899.505 1884.07C916.137 1916.52 895.347 1907.73 882.873 1899.28C596.804 1643.75 209.281 1603.52 51.2781 1615.35C-8.59674 1582.91 6.92633 1527.47 22.1722 1503.81Z")
    static let wingR2 = parse("M1827.88 1700.14C1342.23 1627.13 1040.64 1923.22 950.55 2080.39C933.918 2112.84 954.708 2104.05 967.182 2095.6C1253.25 1840.07 1640.77 1799.85 1798.78 1811.68C1858.65 1779.23 1843.13 1723.8 1827.88 1700.14Z")
    static let wingL2 = parse("M22.1722 1700.14C507.824 1627.13 809.416 1923.22 899.505 2080.39C916.137 2112.84 895.347 2104.05 882.873 2095.6C596.804 1840.07 209.281 1799.85 51.2781 1811.68C-8.59674 1779.23 6.92633 1723.8 22.1722 1700.14Z")

    // Minimal absolute-command SVG path parser (M, L, H, V, C, Z — all this artwork uses).
    private static func parse(_ d: String) -> Path {
        var path = Path()
        var cmd: Character = " "
        var nums: [CGFloat] = []
        var numStr = ""
        var cur = CGPoint.zero

        func flushNum() {
            if !numStr.isEmpty { nums.append(CGFloat(Double(numStr) ?? 0)); numStr = "" }
        }
        func run() {
            flushNum()
            var i = 0
            switch cmd {
            case "M":
                while i + 1 < nums.count + 1 && i + 2 <= nums.count {
                    let pt = CGPoint(x: nums[i], y: nums[i + 1])
                    if i == 0 { path.move(to: pt) } else { path.addLine(to: pt) }
                    cur = pt; i += 2
                }
            case "L":
                while i + 2 <= nums.count {
                    let pt = CGPoint(x: nums[i], y: nums[i + 1])
                    path.addLine(to: pt); cur = pt; i += 2
                }
            case "H":
                while i < nums.count { cur = CGPoint(x: nums[i], y: cur.y); path.addLine(to: cur); i += 1 }
            case "V":
                while i < nums.count { cur = CGPoint(x: cur.x, y: nums[i]); path.addLine(to: cur); i += 1 }
            case "C":
                while i + 6 <= nums.count {
                    let c1 = CGPoint(x: nums[i], y: nums[i + 1])
                    let c2 = CGPoint(x: nums[i + 2], y: nums[i + 3])
                    let to = CGPoint(x: nums[i + 4], y: nums[i + 5])
                    path.addCurve(to: to, control1: c1, control2: c2)
                    cur = to; i += 6
                }
            case "Z":
                path.closeSubpath()
            default: break
            }
            nums.removeAll()
        }

        for ch in d {
            if "MLHVCZ".contains(ch) {
                run()
                cmd = ch
                if ch == "Z" { run() } // Z carries no numbers — apply immediately
            } else if ch == " " || ch == "," {
                flushNum()
            } else if ch == "-" && !numStr.isEmpty {
                flushNum(); numStr = "-"
            } else {
                numStr.append(ch)
            }
        }
        run()
        return path
    }
}
