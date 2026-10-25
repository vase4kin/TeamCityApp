/*
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import UIKit
import SwiftUI

// A storage/network-free control for distinguishing simulator/window behavior
// from Compose rendering. Installed only on task-owned simulators.
@main
@MainActor
final class GeometryControlApp: UIResponder, UIApplicationDelegate {
    func application(_ application: UIApplication, configurationForConnecting session: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        let configuration = UISceneConfiguration(name: "Control", sessionRole: session.role)
        configuration.sceneClass = UIWindowScene.self
        configuration.delegateClass = GeometryControlScene.self
        return configuration
    }
}

@MainActor
final class GeometryControlScene: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?
    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options: UIScene.ConnectionOptions) {
        guard let scene = scene as? UIWindowScene else { return }
        let window = UIWindow(windowScene: scene)
        window.frame = scene.coordinateSpace.bounds
        if ProcessInfo.processInfo.environment["GEOMETRY_HOST"] == "swiftui" {
            window.rootViewController = UIHostingController(rootView: GeometryRepresentable().ignoresSafeArea())
        } else {
            window.rootViewController = GeometryControlViewController()
        }
        self.window = window
        window.makeKeyAndVisible()
    }
}

struct GeometryRepresentable: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> GeometryControlViewController { GeometryControlViewController() }
    func updateUIViewController(_ controller: GeometryControlViewController, context: Context) {}
}

@MainActor
final class GeometryControlViewController: UIViewController {
    private let metrics = UILabel()
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask { .allButUpsideDown }
    override var shouldAutorotate: Bool { true }
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        metrics.text = "UIKit control: loading window metrics"
        metrics.numberOfLines = 0
        metrics.accessibilityIdentifier = "geometry:metrics"
        metrics.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(metrics)
        NSLayoutConstraint.activate([
            metrics.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 12),
            metrics.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -12),
            metrics.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 12)
        ])
    }
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        let value = "UIKit control\nwindow: \(String(describing: view.window?.bounds))\nview: \(view.bounds)\nsafe: \(view.safeAreaInsets)\ninterface orientation: \(view.window?.windowScene?.interfaceOrientation.rawValue ?? -1)"
        if metrics.text != value { metrics.text = value }
    }
}
