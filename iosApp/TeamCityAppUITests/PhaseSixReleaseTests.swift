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
    func testLoginKeyboardRotationAndBackground() throws {
        let app = app()
        app.launch()
        let url = app.descendants(matching: .any).matching(identifier: "auth:url").firstMatch
        XCTAssertTrue(url.waitForExistence(timeout: 25))
        capture("login_portrait", app)
        // Audit app-owned controls before displaying Apple's keyboard. The keyboard
        // is separately exercised below; OS prediction cells are outside app ownership.
        try audit(app)
        url.tap()
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 10))
        capture("login_keyboard", app)
        orient(.landscapeLeft, app: app)
        XCTAssertTrue(url.waitForExistence(timeout: 10))
        XCTAssertTrue(url.isHittable)
        capture("login_landscape_keyboard", app)
        orient(.portrait, app: app)
        XCUIDevice.shared.press(.home)
        app.activate()
        XCTAssertTrue(url.waitForExistence(timeout: 10))
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
        for character in "http://127.0.0.1:18081/context" { url.typeText(String(character)) }
        url.typeText("\n")
        XCTAssertTrue(app.buttons["OK"].waitForExistence(timeout: 10))
        app.buttons["OK"].tap()
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
    }

    func testLaunchPerformance() {
        let app = app()
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
