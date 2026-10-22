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

/// This opt-in test makes read-only requests to the approved public guest server.
final class PhaseTwoLiveSmokeTests: XCTestCase {
    private func element(_ app: XCUIApplication, _ identifier: String) -> XCUIElement {
        app.descendants(matching: .any).matching(identifier: identifier).firstMatch
    }
    private func capture(_ name: String, _ app: XCUIApplication) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        let theme = ProcessInfo.processInfo.environment["TEAMCITY_UI_EVIDENCE_THEME"] ?? "light"
        attachment.name = name.replacingOccurrences(of: "_light", with: "_" + theme)
        attachment.lifetime = .keepAlways
        add(attachment)
    }
    func testLoginValidationAndExplicitSslConsent() {
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchEnvironment["TEAMCITY_UI_TEST_VAULT"] = UUID().uuidString
        app.launchArguments += ["-AppleLanguages","(en)","-AppleLocale","en_US"]
        app.launch()
        let submit = element(app,"login:submit")
        XCTAssertTrue(submit.waitForExistence(timeout: 25))
        findByScrolling(app,"login:submit").press(forDuration: 0.2)
        XCTAssertTrue(element(app,"auth:error").waitForExistence(timeout: 5))
        capture("phase2_login_validation",app)
        findByScrolling(app,"auth:ssl").press(forDuration: 0.2)
        XCTAssertTrue(element(app,"auth:warning").waitForExistence(timeout: 5))
        capture("phase2_ssl_consent",app)
        app.buttons["Cancel"].tap()
        XCTAssertTrue(element(app,"auth:ssl").waitForExistence(timeout: 5))
        XCTAssertFalse(element(app,"auth:warning").exists)
        findByScrolling(app,"auth:ssl").press(forDuration: 0.2)
        XCTAssertTrue(element(app,"auth:warning").waitForExistence(timeout: 5))
        app.buttons["OK"].tap()
        findByScrolling(app,"auth:ssl").press(forDuration: 0.2)
        XCTAssertFalse(element(app,"auth:warning").exists)
    }
    func testHttpConsentAndUnreachableServerKeepTheAccountSignedOut() {
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchEnvironment["TEAMCITY_UI_TEST_VAULT"] = UUID().uuidString
        app.launchArguments += ["-AppleLanguages","(en)","-AppleLocale","en_US"]
        app.launch()
        XCTAssertTrue(element(app,"auth:guest").waitForExistence(timeout: 25))
        element(app,"auth:guest").press(forDuration: 0.2)
        let url = element(app,"auth:url")
        url.tap()
        // Give Compose's native text bridge an event boundary per key; XCTest's
        // batched synthetic typing can drop characters while the field recomposes.
        for _ in 0..<8 { url.typeText(XCUIKeyboardKey.delete.rawValue) }
        for character in "http://127.0.0.1:1" { url.typeText(String(character)) }
        XCTAssertEqual(url.value as? String,"http://127.0.0.1:1")
        url.typeText("\n")
        XCTAssertTrue(element(app,"auth:warning").waitForExistence(timeout: 5))
        capture("phase2_http_consent",app)
        app.buttons["Cancel"].tap()
        findByScrolling(app,"login:submit").press(forDuration: 0.2)
        XCTAssertTrue(element(app,"auth:warning").waitForExistence(timeout: 5))
        app.buttons["OK"].tap()
        XCTAssertTrue(element(app,"auth:error").waitForExistence(timeout: 35))
        capture("phase2_unreachable_server",app)
        element(app,"login:accounts").tap()
        XCTAssertTrue(app.staticTexts["No saved accounts"].waitForExistence(timeout: 10))
        capture("phase2_empty_accounts",app)
    }
    private func waitUntilSettled(_ target: XCUIElement) {
        // Compose scroll deceleration may outlive XCTest's UIKit-idle check.
        var lastFrame = target.frame
        var unchangedSince = Date()
        let settled = NSPredicate { _,_ in
            let frame = target.frame
            if frame != lastFrame { lastFrame = frame; unchangedSince = Date() }
            return Date().timeIntervalSince(unchangedSince) >= 0.6
        }
        XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: settled, object: target)], timeout: 5), .completed)
    }
    private func findByScrolling(_ app: XCUIApplication, _ identifier: String) -> XCUIElement {
        let target = element(app,identifier)
        for _ in 0..<40 {
            if target.exists && target.isHittable {
                waitUntilSettled(target)
                return target
            }
            app.swipeUp(velocity: .slow)
        }
        return target
    }
    func testGuestJourneyPaginationAndSecureRelaunch() throws {
        guard ProcessInfo.processInfo.environment["TEAMCITY_LIVE_UI"] == "1" else {
            throw XCTSkip("Live public-server verification is opt-in.")
        }
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchEnvironment["TEAMCITY_UI_TEST_VAULT"] = UUID().uuidString
        app.launchArguments += ["-AppleLanguages","(en)","-AppleLocale","en_US"]
        app.launch()
        XCTAssertTrue(element(app,"login:demo").waitForExistence(timeout: 25))
        capture("phase2_login_light",app)
        findByScrolling(app,"login:demo").press(forDuration: 0.2)
        capture("phase2_demo_confirmation",app)
        XCTAssertTrue(app.buttons["Try it out"].waitForExistence(timeout: 5))
        app.buttons["Try it out"].tap()
        let kotlin = element(app,"navigation:row:project:Kotlin")
        XCTAssertTrue(kotlin.waitForExistence(timeout: 40))
        capture("phase2_projects_light",app)
        kotlin.tap()
        let configuration = findByScrolling(app,"navigation:row:configuration:bt446")
        XCTAssertTrue(configuration.exists)
        capture("phase2_configurations_light",app)
        configuration.tap()
        let firstBuild = element(app,"history:list").descendants(matching: .any).matching(NSPredicate(format:"identifier BEGINSWITH %@", "history:build:")).firstMatch
        XCTAssertTrue(firstBuild.waitForExistence(timeout: 40))
        capture("phase2_history_light",app)
        // Scrolling to the bottom triggers opaque nextHref loading through shared Paging.
        for _ in 0..<6 { app.swipeUp(velocity: .slow) }
        capture("phase2_history_pagination_light",app)
        let listBounds = element(app,"history:list").frame
        let build = app.buttons.matching(NSPredicate(format:"identifier BEGINSWITH %@", "history:build:")).allElementsBoundByIndex.first {
            $0.isHittable && $0.frame.height > 60 && $0.frame.minY >= listBounds.minY && $0.frame.maxY <= listBounds.maxY
        }
        XCTAssertNotNil(build)
        waitUntilSettled(build!)
        build!.press(forDuration: 0.2)
        XCTAssertTrue(element(app,"overview:screen").waitForExistence(timeout: 40))
        XCTAssertTrue(element(app,"overview:row:Result").waitForExistence(timeout: 20))
        capture("phase2_overview_light",app)
        app.terminate()
        app.launch()
        // The account comes from Keychain; only its public reference navigation was persisted.
        XCTAssertTrue(element(app,"overview:row:Result").waitForExistence(timeout: 40))
        capture("phase2_overview_relaunched",app)
        XCUIDevice.shared.orientation = .landscapeLeft
        capture("phase2_overview_expanded",app)
        XCUIDevice.shared.orientation = .portrait
        waitUntilSettled(app.buttons["Back"])
        app.buttons["Back"].press(forDuration: 0.2)
        XCTAssertTrue(element(app,"history:back").waitForExistence(timeout: 10))
        element(app,"history:back").tap()
        XCTAssertTrue(element(app,"navigation:up").waitForExistence(timeout: 10))
        element(app,"navigation:up").tap()
        XCTAssertTrue(kotlin.waitForExistence(timeout: 10))
        element(app,"navigation:up").tap()
        let signOut = element(app,"accounts:sign-out")
        XCTAssertTrue(signOut.waitForExistence(timeout: 10))
        capture("phase2_saved_accounts",app)
        signOut.tap()
        XCTAssertTrue(element(app,"login:accounts").waitForExistence(timeout: 10))
        capture("phase2_signed_out",app)
        element(app,"login:accounts").tap()
        let saved = app.buttons.matching(NSPredicate(format:"identifier BEGINSWITH %@", "accounts:activate:")).firstMatch
        XCTAssertTrue(saved.waitForExistence(timeout: 10))
        saved.tap()
        XCTAssertTrue(kotlin.waitForExistence(timeout: 30))
        capture("phase2_account_reselected",app)
    }
}

