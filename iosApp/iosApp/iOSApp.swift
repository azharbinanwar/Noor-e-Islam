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

#if DEBUG
private let buildType = BuildType.debug
#else
private let buildType = BuildType.release
#endif

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @State private var showSplash = true

    init() {
        DIKt.startKoinForIos(edition: AppEdition.main, build: buildType)
        // Set before launch finishes, so a tap that cold-starts the app is still delivered.
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
        NotificationScheduler.shared.start() // build the reminder window + re-arm on any change
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

/// Reads one key off a tapped notification and hands the raw string to shared. Knows nothing about
/// what's inside it — shared decodes the route and the nav host acts on it.
private final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegate()

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let payload = response.notification.request.content.userInfo[NotificationRouteKt.NOTIF_ROUTE_KEY] as? String
        PendingNavigation.shared.offer(payload: payload)
        completionHandler()
    }

    // Reminders are worth seeing even with the app open.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }
}

// ───────────────────────── splash: "Rings First" ─────────────────────────
// The four rings ripple outward from the center building the frame, then the mosque
// rises up into its place. Timings mirror the Android AVD (avd_splash.xml).

private struct SplashView: View {
    let onFinished: () -> Void
    @State private var r4 = false
    @State private var r3 = false
    @State private var r2 = false
    @State private var r1 = false
    @State private var mosque = false

    var body: some View {
        ZStack {
            Color(red: 0x1E / 255.0, green: 0x7D / 255.0, blue: 0x55 / 255.0)
                .ignoresSafeArea()
            ZStack {
                LogoShape(.r1).fill(.white, style: FillStyle(eoFill: true))
                    .opacity(r1 ? 0.6 : 0).scaleEffect(r1 ? 1 : 0.55)
                LogoShape(.r2).fill(.white, style: FillStyle(eoFill: true))
                    .opacity(r2 ? 0.8 : 0).scaleEffect(r2 ? 1 : 0.55)
                LogoShape(.r3).fill(.white, style: FillStyle(eoFill: true))
                    .opacity(r3 ? 0.9 : 0).scaleEffect(r3 ? 1 : 0.55)
                LogoShape(.r4).fill(.white, style: FillStyle(eoFill: true))
                    .opacity(r4 ? 1 : 0).scaleEffect(r4 ? 1 : 0.55)
                LogoShape(.mosque).fill(.white)
                    .opacity(mosque ? 1 : 0)
                    .offset(y: mosque ? 0 : 20)
            }
            .frame(width: 160, height: 160)
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.35)) { r4 = true }
            withAnimation(.easeOut(duration: 0.35).delay(0.10)) { r3 = true }
            withAnimation(.easeOut(duration: 0.35).delay(0.20)) { r2 = true }
            withAnimation(.easeOut(duration: 0.35).delay(0.30)) { r1 = true }
            withAnimation(.easeOut(duration: 0.5).delay(0.5)) { mosque = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { onFinished() }
        }
    }
}

// The logo's five SVG paths, parsed once and scaled to fit whatever frame the shape gets.
private struct LogoShape: Shape {
    enum Part { case r1, r2, r3, r4, mosque }
    let part: Part
    init(_ part: Part) { self.part = part }

    // source artwork viewBox
    private static let vb: CGFloat = 799

    func path(in rect: CGRect) -> Path {
        let base: Path
        switch part {
        case .r1: base = Self.ring1
        case .r2: base = Self.ring2
        case .r3: base = Self.ring3
        case .r4: base = Self.ring4
        case .mosque: base = Self.mosqueP
        }
        let scale = min(rect.width, rect.height) / Self.vb
        let t = CGAffineTransform(translationX: rect.midX - Self.vb * scale / 2,
                                  y: rect.midY - Self.vb * scale / 2)
            .scaledBy(x: scale, y: scale)
        return base.applying(t)
    }

    static let ring1 = parse("M399.5 0C620.138 5.47636e-07 799 178.862 799 399.5C799 620.138 620.138 799 399.5 799C178.862 799 5.4792e-07 620.138 0 399.5C0 178.862 178.862 0 399.5 0ZM399.2 41.3604C201.924 41.3604 42.0001 201.284 42 398.56C42.0001 595.836 201.924 755.76 399.2 755.76C596.476 755.76 756.4 595.836 756.4 398.56C756.4 201.284 596.476 41.3605 399.2 41.3604Z")
    static let ring2 = parse("M399.2 42C596.476 42.0002 756.4 201.923 756.4 399.199C756.4 596.475 596.476 756.399 399.2 756.399C201.924 756.399 42.0001 596.475 42 399.199C42.0001 201.923 201.924 42 399.2 42ZM398.9 85.2402C224.986 85.2402 84 226.226 84 400.14C84.0003 574.055 224.986 715.04 398.9 715.04C572.815 715.04 713.8 574.055 713.8 400.14C713.8 226.226 572.815 85.2407 398.9 85.2402Z")
    static let ring3 = parse("M398.9 84C572.815 84.0004 713.8 224.986 713.8 398.899C713.8 572.814 572.815 713.8 398.9 713.8C224.986 713.8 84.0003 572.814 84 398.899C84 224.985 224.986 84 398.9 84ZM398.6 125.359C248.047 125.36 126 247.406 126 397.959C126 548.511 248.047 670.559 398.6 670.56C549.152 670.56 671.2 548.511 671.2 397.959C671.2 247.406 549.151 125.359 398.6 125.359Z")
    static let ring4 = parse("M399.6 127C550.152 127 672.2 249.047 672.2 399.6C672.2 550.152 550.152 672.2 399.6 672.2C249.047 672.2 127 550.152 127 399.6C127 249.047 249.047 127 399.6 127ZM398.66 170.24C271.469 170.24 168.36 273.349 168.36 400.54C168.36 527.731 271.469 630.84 398.66 630.84C525.851 630.84 628.96 527.731 628.96 400.54C628.96 273.349 525.851 170.24 398.66 170.24Z")
    static let mosqueP = parse("M397.575 262C383.555 266.824 373.48 280.127 373.48 295.784C373.481 315.512 389.473 331.504 409.2 331.504C424.858 331.504 438.161 321.429 442.984 307.408C441.89 329.005 424.95 346.417 403.56 348.259V362.024C408.59 363.485 411.811 366.154 414.21 369.529C416.938 373.366 418.625 378.18 420.748 382.858C422.899 387.597 425.565 392.396 430.311 396.596C432.554 398.58 435.277 400.443 438.652 402.102C508.131 416.905 559.599 470.496 559.6 534.342C559.6 547.195 557.511 559.632 553.612 571.421C512.725 608.542 458.435 631.163 398.86 631.163C340.592 631.163 287.378 609.524 246.816 573.843C242.385 561.34 240 548.079 240 534.342C240 469.542 293.018 415.304 364.073 401.463C366.882 399.97 369.209 398.33 371.169 396.596C375.916 392.396 378.582 387.597 380.732 382.858C382.855 378.18 384.541 373.366 387.269 369.529C389.669 366.154 392.89 363.485 397.92 362.024V348.38C374.912 347.396 356.561 328.435 356.561 305.185C356.561 282.05 374.729 263.158 397.575 262Z")

    // Minimal absolute-command SVG path parser (M, L, H, V, C, Z), exponent-aware ("5.47e-07").
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
                while i + 2 <= nums.count {
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
            } else if ch == "-" && !numStr.isEmpty && numStr.last != "e" && numStr.last != "E" {
                flushNum(); numStr = "-"
            } else {
                numStr.append(ch)
            }
        }
        run()
        return path
    }
}
