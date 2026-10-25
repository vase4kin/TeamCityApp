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

import XCTest

/// Physical tests may use an explicit private interface; management tests keep
/// their independent loopback-only URL and opt-in mutation server.
var teamCityReadOnlyFixtureBaseURL: String {
    let value = ProcessInfo.processInfo.environment["TEAMCITY_FIXTURE_BASE_URL"] ?? "http://127.0.0.1:18081/context"
    let url = URLComponents(string: value)
    let host = url?.host ?? ""
    let parts = host.split(separator: ".", omittingEmptySubsequences: false)
    let octets = parts.compactMap { Int($0) }
    let ipv4 = parts.count == 4 && octets.count == 4 && octets.allSatisfy { (0...255).contains($0) } && octets.map(String.init).joined(separator: ".") == host
    let local = ipv4 && (octets[0] == 127 || octets[0] == 10 || (octets[0] == 192 && octets[1] == 168) || (octets[0] == 172 && (16...31).contains(octets[1])))
    precondition(local && url?.scheme == "http" && url?.port == 18081 && url?.path == "/context" && url?.user == nil && url?.password == nil && url?.query == nil && url?.fragment == nil, "Read-only fixtures require a private IPv4 interface on port 18081 at /context")
    return value
}

func approveReadOnlyFixtureNetworkIfPresented() {
    let alert = XCUIApplication(bundleIdentifier: "com.apple.springboard").alerts.firstMatch
    if alert.waitForExistence(timeout: 3) && alert.label.localizedCaseInsensitiveContains("local network") && alert.buttons["Allow"].exists {
        alert.buttons["Allow"].tap()
    }
}

/// Release qualification uses an isolated app ID on physical hardware. It never rates,
/// submits a real build, or changes a user's existing account vault.
final class PhaseSixReleaseTests: XCTestCase {
    override func tearDown() {
        XCUIDevice.shared.orientation = .portrait
        super.tearDown()
    }
    private func app(preview: Bool = false) -> XCUIApplication {
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchEnvironment["TEAMCITY_UI_TEST_VAULT"] = UUID().uuidString
        if preview {
            app.launchEnvironment["PHASE1_STATE"] = "available"
            app.launchEnvironment["PHASE1_THEME"] = ProcessInfo.processInfo.environment["TEAMCITY_UI_EVIDENCE_THEME"] ?? "light"
        }
        app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        return app
    }
    private func capture(_ name: String, _ app: XCUIApplication) {
        // Capture the display: app-window crops can be offset during landscape
        // keyboard presentation on the current simulator toolchain.
        let shot = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        shot.name = "phase6_" + name
        shot.lifetime = .keepAlways
        add(shot)
    }
    private func orient(_ orientation: UIDeviceOrientation, app: XCUIApplication) {
        XCUIDevice.shared.orientation = orientation
        let landscape = orientation == .landscapeLeft || orientation == .landscapeRight
        let settled = NSPredicate { _, _ in (app.frame.width > app.frame.height) == landscape }
        XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: settled, object: app)], timeout: 10), .completed)
    }
    private func prepareLogin(_ app: XCUIApplication) {
        let url = app.descendants(matching: .any).matching(identifier: "auth:url").firstMatch
        if url.waitForExistence(timeout: 2) { return }
        // Release deliberately ignores the Debug test-vault override. A validation
        // installation can retain a guest session; sign out through its normal UI
        // without deleting saved accounts or changing production storage behavior.
        let account = app.descendants(matching: .any).matching(identifier: "home:account").firstMatch
        XCTAssertTrue(account.waitForExistence(timeout: 25))
        account.tap()
        let signOut = app.descendants(matching: .any).matching(identifier: "accounts:sign-out").firstMatch
        XCTAssertTrue(signOut.waitForExistence(timeout: 10))
        signOut.tap()
        XCTAssertTrue(url.waitForExistence(timeout: 10))
    }
    func testLoginKeyboardRotationAndBackground() throws {
        let app = app()
        app.launch()
        prepareLogin(app)
        let url = app.descendants(matching: .any).matching(identifier: "auth:url").firstMatch
        XCTAssertTrue(url.waitForExistence(timeout: 25))
        capture("login_portrait", app)
        // Audit app-owned controls before displaying Apple's keyboard. The keyboard
        // is separately exercised below; OS prediction cells are outside app ownership.
        try audit(app)
        url.tap()
        // Third-party keyboards can expose Key elements without a Keyboard
        // container. Require visible keys and prove the field accepts input.
        let keyboard = NSPredicate { _, _ in app.keyboards.firstMatch.exists || app.keys.firstMatch.exists }
        XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: keyboard, object: app)], timeout: 10), .completed)
        url.typeText("e")
        let entered = NSPredicate(format: "value == %@", "https://e")
        XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: entered, object: url)], timeout: 5), .completed)
        capture("login_keyboard", app)
        orient(.landscapeLeft, app: app)
        XCTAssertTrue(url.waitForExistence(timeout: 10))
        XCTAssertTrue(url.isHittable)
        capture("login_landscape_keyboard", app)
        orient(.portrait, app: app)
        XCUIDevice.shared.press(.home)
        app.activate()
        XCTAssertTrue(url.waitForExistence(timeout: 10))
        XCTAssertEqual(url.value as? String, "https://e")
        capture("login_foreground", app)
    }
    func testAboutOrientationAndAccessibleActions() throws {
        let app = app(preview: true)
        app.launch()
        XCTAssertTrue(app.staticTexts["https://teamcity.example"].waitForExistence(timeout: 25))
        capture("about_portrait", app)
        orient(.landscapeLeft, app: app)
        XCTAssertTrue(app.staticTexts["Application details"].waitForExistence(timeout: 10))
        capture("about_landscape", app)
        orient(.portrait, app: app)
        let privacy = app.staticTexts["Privacy policy"]
        for _ in 0..<8 {
            if privacy.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(privacy.isHittable)
        capture("about_actions", app)
        try audit(app)
    }

    private func audit(_ app: XCUIApplication) throws {
        if #available(iOS 17.0, *) {
            let tree = XCTAttachment(string: app.debugDescription)
            tree.name = "phase6_accessibility_tree"
            tree.lifetime = .keepAlways
            add(tree)
            try app.performAccessibilityAudit(for: [.contrast, .hitRegion, .sufficientElementDescription, .trait]) { issue in
                let details = XCTAttachment(string: issue.detailedDescription + "\n" + (issue.element?.debugDescription ?? "No element") + "\n" + app.debugDescription)
                details.name = "phase6_accessibility_issue"
                details.lifetime = .keepAlways
                self.add(details)
                return false
            }
        }
    }

    func testHomeAndBuildAccessibility() throws {
        guard ProcessInfo.processInfo.environment["TEAMCITY_FIXTURE_UI"] == "1" else { throw XCTSkip("Read-only local fixture is opt-in") }
        let app = app()
        app.launch()
        func element(_ id: String) -> XCUIElement { app.descendants(matching: .any).matching(identifier: id).firstMatch }
        func tap(_ id: String) {
            XCTAssertTrue(element(id).waitForExistence(timeout: 25))
            element(id).press(forDuration: 0.2)
        }
        tap("auth:guest")
        let url = element("auth:url")
        url.tap()
        for _ in 0..<8 { url.typeText(XCUIKeyboardKey.delete.rawValue) }
        for character in teamCityReadOnlyFixtureBaseURL { url.typeText(String(character)) }
        url.typeText("\n")
        XCTAssertTrue(app.buttons["OK"].waitForExistence(timeout: 10))
        app.buttons["OK"].tap()
        approveReadOnlyFixtureNetworkIfPresented()
        for tab in ["Favorites", "Activity", "Projects"] {
            tap("home:tab:" + tab)
            capture("accessible_home_" + tab.lowercased(), app)
            try audit(app)
        }
        tap("navigation:row:configuration:Demo_Build")
        XCTAssertTrue(element("history:build:1").waitForExistence(timeout: 25))
        capture("accessible_history", app)
        try audit(app)
        tap("history:build:1")
        XCTAssertTrue(element("overview:screen").waitForExistence(timeout: 25))
        capture("accessible_overview", app)
        try audit(app)
        let agent = element("overview:row:Agent")
        // Partly clipped rows must not expose a tiny action. Once revealed by
        // scrolling, their label and full actionable frame must be available.
        if agent.exists && agent.elementType == .button { XCTAssertGreaterThanOrEqual(agent.frame.height, 44) }
        for _ in 0..<8 {
            if agent.exists && agent.elementType == .button && agent.isHittable && agent.frame.height >= 44 { break }
            app.swipeUp()
        }
        XCTAssertTrue(agent.waitForExistence(timeout: 10))
        XCTAssertEqual(agent.elementType, .button)
        XCTAssertGreaterThanOrEqual(agent.frame.height, 44)
        XCTAssertTrue(agent.label.contains("macOS agent"))
        capture("accessible_overview_agent_revealed", app)
        try audit(app)
    }

    func testLaunchPerformance() {
        let app = app()
        app.launch()
        prepareLogin(app)
        app.terminate()
        let options = XCTMeasureOptions()
        options.iterationCount = 5
        measure(metrics: [XCTApplicationLaunchMetric(waitUntilResponsive: true)], options: options) {
            app.launch()
            XCTAssertTrue(app.descendants(matching: .any).matching(identifier: "auth:url").firstMatch.waitForExistence(timeout: 25))
            app.terminate()
        }
    }
    func testScrollPerformanceAndMemory() {
        let app = app(preview: true)
        app.launch()
        XCTAssertTrue(app.staticTexts["https://teamcity.example"].waitForExistence(timeout: 25))
        let options = XCTMeasureOptions()
        options.iterationCount = 5
        measure(metrics: [XCTClockMetric(), XCTMemoryMetric(application: app), XCTOSSignpostMetric.scrollDecelerationMetric], options: options) {
            app.swipeUp()
            app.swipeDown()
        }
    }
}
