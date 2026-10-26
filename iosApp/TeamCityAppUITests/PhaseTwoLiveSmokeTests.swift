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

/// Walk shared first-run education through its normal public controls.
private func dismissSharedEducation(_ app: XCUIApplication) {
    for _ in 0..<8 {
        let ids = ["home:dismiss-prompt", "history:prompt-dismiss", "details:dismiss-prompt", "navigation:rate-cancel"]
        guard let target = ids.map({ app.descendants(matching: .any).matching(identifier: $0).firstMatch }).first(where: { $0.exists && $0.isHittable }) else { return }
        target.press(forDuration: 0.2)
        _ = target.waitForNonExistence(timeout: 5)
    }
}

private func waitForSharedContent(_ app: XCUIApplication, _ identifier: String, timeout: TimeInterval = 25) -> Bool {
    let target = app.descendants(matching: .any).matching(identifier: identifier).firstMatch
    let deadline = Date().addingTimeInterval(timeout)
    repeat {
        dismissSharedEducation(app)
        if target.waitForExistence(timeout: 0.5) { return true }
    } while Date() < deadline
    return false
}

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
        XCTAssertTrue(app.staticTexts["Add an account to connect to TeamCity"].waitForExistence(timeout: 10))
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
        XCTAssertTrue(element(app,"home:tab:Activity").waitForExistence(timeout: 40))
        element(app,"home:tab:Activity").tap()
        element(app,"home:filter:RunningAll").tap()
        XCTAssertTrue(element(app,"running_builds:list").waitForExistence(timeout: 40))
        capture("phase3_live_running_light",app)
        element(app,"home:tab:Queue").tap()
        element(app,"home:filter:QueuedAll").tap()
        XCTAssertTrue(element(app,"build_queue:list").waitForExistence(timeout: 40))
        capture("phase3_live_queue_light",app)
        element(app,"home:tab:Agents").tap()
        XCTAssertTrue(element(app,"agents:list").waitForExistence(timeout: 40))
        capture("phase3_live_agents_light",app)
        element(app,"home:tab:Projects").tap()
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
        let buildId = app.buttons.matching(NSPredicate(format:"identifier BEGINSWITH %@", "history:build:")).allElementsBoundByIndex.first {
            $0.isHittable && $0.frame.height > 60 && $0.frame.minY >= listBounds.minY && $0.frame.maxY <= listBounds.maxY
        }?.identifier
        XCTAssertNotNil(buildId)
        // Paging and deceleration can change the accessibility query's indices.
        // Keep the selected build identity while waiting for its frame to settle.
        let build = app.buttons.matching(identifier: buildId!).firstMatch
        waitUntilSettled(build)
        build.press(forDuration: 0.2)
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
        let up = app.buttons["Navigate up"].exists ? app.buttons["Navigate up"] : app.buttons["Back"]
        waitUntilSettled(up)
        up.press(forDuration: 0.2)
        XCTAssertTrue(element(app,"history:back").waitForExistence(timeout: 10))
        element(app,"history:back").tap()
        XCTAssertTrue(element(app,"navigation:up").waitForExistence(timeout: 10))
        element(app,"navigation:up").tap()
        XCTAssertTrue(kotlin.waitForExistence(timeout: 10))
        element(app,"home:account").tap()
        let signOut = element(app,"accounts:sign-out")
        XCTAssertTrue(signOut.waitForExistence(timeout: 10))
        capture("phase2_saved_accounts",app)
        signOut.tap()
        XCTAssertTrue(element(app,"login:accounts").waitForExistence(timeout: 10))
        capture("phase2_signed_out",app)
        element(app,"login:accounts").tap()
        let saved = app.buttons.matching(NSPredicate(format:"identifier BEGINSWITH %@", "drawer:account:")).firstMatch
        XCTAssertTrue(saved.waitForExistence(timeout: 10))
        saved.tap()
        XCTAssertTrue(kotlin.waitForExistence(timeout: 30))
        capture("phase2_account_reselected",app)
    }
}


/// Deterministic HTTP-backed coverage of the normal iOS app; no preview screen is substituted.
final class PhaseThreeBrowsingSmokeTests: XCTestCase {
    private func element(_ app: XCUIApplication, _ id: String) -> XCUIElement {
        app.descendants(matching: .any).matching(identifier: id).firstMatch
    }
    private func capture(_ name: String, _ app: XCUIApplication) {
        let bar = element(app, "screen:app-bar")
        // Modal authentication warnings and initial Login hide/omit the app bar.
        // Settle the visible app frame there; keep header checks on normal visits.
        let anchor = bar.exists ? bar : app
        var lastFrame = anchor.frame
        var unchangedSince = Date()
        let settled = NSPredicate { _, _ in
            let frame = anchor.frame
            if frame != lastFrame { lastFrame = frame; unchangedSince = Date() }
            return Date().timeIntervalSince(unchangedSince) >= 0.6
        }
        XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: settled, object: anchor)], timeout: 5), .completed)
        if bar.exists && XCUIDevice.shared.orientation == .portrait {
            assertNativeHeaderSafeArea(bar, app: app)
        }
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        let theme = ProcessInfo.processInfo.environment["TEAMCITY_UI_EVIDENCE_THEME"] ?? "light"
        attachment.name = "phase3_" + name + "_" + theme
        attachment.lifetime = .keepAlways
        add(attachment)
    }
    private func tap(_ app: XCUIApplication, _ id: String, scroll: Bool = false) {
        dismissSharedEducation(app)
        let target = element(app, id)
        for _ in 0..<20 {
            if target.exists { break }
            dismissSharedEducation(app)
            if target.waitForExistence(timeout: 0.5) { break }
        }
        if scroll {
            for _ in 0..<12 {
                if target.exists && target.isHittable { break }
                app.swipeUp(velocity: .slow)
            }
        }
        XCTAssertTrue(target.waitForExistence(timeout: 25), id)
        if scroll {
            var lastFrame = target.frame
            var unchangedSince = Date()
            let settled = NSPredicate { _, _ in
                let frame = target.frame
                if frame != lastFrame { lastFrame = frame; unchangedSince = Date() }
                return Date().timeIntervalSince(unchangedSince) >= 0.6
            }
            XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: settled, object: target)], timeout: 5), .completed)
        }
        target.press(forDuration: 0.2)
    }
    private func back(_ app: XCUIApplication) {
        let back = app.buttons["Navigate up"].exists ? app.buttons["Navigate up"].firstMatch : app.buttons["Back"].firstMatch
        XCTAssertTrue(back.waitForExistence(timeout: 10))
        back.press(forDuration: 0.2)
    }
    private func acknowledgeHomePrompt(_ app: XCUIApplication, _ prompt: String) {
        let coachmark = element(app, "home:coachmark:" + prompt)
        XCTAssertTrue(coachmark.waitForExistence(timeout: 10), "First-use prompt: " + prompt)
        capture("first_use_" + prompt.lowercased(), app)
        let dismiss = element(app, "home:dismiss-prompt")
        XCTAssertTrue(dismiss.isHittable)
        dismiss.press(forDuration: 0.2)
        XCTAssertTrue(coachmark.waitForNonExistence(timeout: 10))
    }
    private func fixtureApp(_ suffix: String = "") -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["TEAMCITY_UI_TEST_VAULT"] = UUID().uuidString
        app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        app.launch()
        prepareValidationLogin(app)
        tap(app, "auth:guest")
        let url = element(app, "auth:url")
        url.tap()
        for _ in 0..<8 { url.typeText(XCUIKeyboardKey.delete.rawValue) }
        for character in teamCityReadOnlyFixtureBaseURL + suffix { url.typeText(String(character)) }
        XCTAssertEqual(url.value as? String, teamCityReadOnlyFixtureBaseURL + suffix)
        url.typeText("\n")
        XCTAssertTrue(element(app, "auth:warning").waitForExistence(timeout: 10))
        app.buttons["OK"].tap()
        approveReadOnlyFixtureNetworkIfPresented()
        acknowledgeHomePrompt(app, "Drawer")
        dismissSharedEducation(app)
        XCTAssertTrue(element(app, "home:tab:Favorites").waitForExistence(timeout: 30))
        return app
    }
    func testSharedAccountAddDuplicateAndConfirmedRemoval() throws {
        guard ProcessInfo.processInfo.environment["TEAMCITY_FIXTURE_UI"] == "1" else { throw XCTSkip("Read-only fixture is opt-in") }
        continueAfterFailure = false
        let app = fixtureApp("/manage-start")
        func enter(_ suffix: String) {
            tap(app, "auth:guest")
            let url = element(app, "auth:url")
            url.tap()
            for _ in 0..<8 { url.typeText(XCUIKeyboardKey.delete.rawValue) }
            for character in teamCityReadOnlyFixtureBaseURL + suffix { url.typeText(String(character)) }
            url.typeText("\n")
        }
        func discard() {
            back(app)
            XCTAssertTrue(element(app, "create-account:discard").waitForExistence(timeout: 10))
            app.buttons["Discard"].tap()
        }
        tap(app, "home:account")
        tap(app, "drawer:add", scroll: true)
        enter("/manage-start")
        XCTAssertTrue(element(app, "auth:error").waitForExistence(timeout: 15))
        XCTAssertTrue(element(app, "create-account:form").exists)
        capture("add_account_duplicate", app)
        discard()
        tap(app, "drawer:add", scroll: true)
        enter("/manage-added")
        XCTAssertTrue(element(app, "auth:warning").waitForExistence(timeout: 10))
        capture("add_account_http_consent", app)
        app.buttons["OK"].tap()
        XCTAssertTrue(element(app, "home:account").waitForExistence(timeout: 30))
        tap(app, "home:account")
        tap(app, "drawer:manage", scroll: true)
        XCTAssertTrue(element(app, "accounts:list").waitForExistence(timeout: 15))
        let remove = app.buttons.matching(NSPredicate(format: "identifier BEGINSWITH %@ AND identifier ENDSWITH %@", "accounts:row:", ":remove"))
        XCTAssertEqual(remove.count, 2)
        capture("manage_accounts_two_saved", app)
        let inactive = remove.allElementsBoundByIndex.first { $0.label.contains("/manage-start/") }
        XCTAssertNotNil(inactive)
        inactive!.tap()
        XCTAssertTrue(element(app, "accounts:dialog").waitForExistence(timeout: 10))
        app.buttons["Cancel"].tap()
        XCTAssertEqual(remove.count, 2)
        inactive!.tap()
        XCTAssertTrue(element(app, "accounts:dialog").waitForExistence(timeout: 10))
        app.buttons["Remove"].tap()
        let oneRemaining = NSPredicate { _, _ in remove.count == 1 }
        XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: oneRemaining, object: app)], timeout: 15), .completed)
        XCTAssertTrue(remove.firstMatch.label.contains("/manage-added/"))
        capture("manage_accounts_inactive_removed", app)
        back(app)
        tap(app, "accounts:sign-out", scroll: true)
        XCTAssertTrue(element(app, "auth:url").waitForExistence(timeout: 20))
        tap(app, "login:accounts")
        tap(app, "drawer:manage", scroll: true)
        XCTAssertTrue(element(app, "accounts:list").waitForExistence(timeout: 10))
        XCTAssertEqual(remove.count, 1)
        remove.firstMatch.tap()
        XCTAssertTrue(element(app, "accounts:dialog").waitForExistence(timeout: 10))
        app.buttons["Remove"].tap()
        XCTAssertTrue(element(app, "auth:url").waitForExistence(timeout: 20))
        capture("manage_accounts_last_removed_login", app)
    }

    func testAccountFlowMatchesAndroidAndHistoryHasOneBuildVisit() throws {
        guard ProcessInfo.processInfo.environment["TEAMCITY_FIXTURE_UI"] == "1" else { throw XCTSkip("Read-only fixture is opt-in") }
        continueAfterFailure = false
        let app = fixtureApp("/account-review")
        func audit() throws {
            if #available(iOS 17.0, *) { try app.performAccessibilityAudit(for: [.contrast, .hitRegion, .sufficientElementDescription, .trait]) }
        }
        tap(app, "home:account")
        XCTAssertTrue(element(app, "drawer:manage").waitForExistence(timeout: 15))
        capture("account_android_parity", app)
        try audit()
        tap(app, "drawer:add", scroll: true)
        XCTAssertTrue(element(app, "create-account:form").waitForExistence(timeout: 15))
        XCTAssertFalse(element(app, "login:submit").exists)
        XCTAssertTrue(app.staticTexts["Add user account"].exists)
        capture("add_account_distinct", app)
        try audit()
        tap(app, "create-account:submit", scroll: true)
        XCTAssertTrue(element(app, "auth:error").waitForExistence(timeout: 10))
        capture("add_account_validation", app)
        try audit()
        back(app)
        XCTAssertTrue(element(app, "create-account:discard").waitForExistence(timeout: 10))
        app.buttons["Discard"].tap()
        tap(app, "drawer:manage", scroll: true)
        XCTAssertTrue(element(app, "accounts:list").waitForExistence(timeout: 15))
        XCTAssertTrue(app.staticTexts["Manage Accounts"].exists)
        capture("manage_accounts_distinct", app)
        let server = app.staticTexts.matching(NSPredicate(format: "identifier BEGINSWITH %@ AND identifier ENDSWITH %@", "accounts:row:", ":url")).firstMatch
        XCTAssertTrue(server.label.hasPrefix("Server url,"))
        try audit()
        tap(app, "accounts:add", scroll: true)
        XCTAssertTrue(element(app, "create-account:form").waitForExistence(timeout: 15))
        back(app)
        XCTAssertTrue(element(app, "create-account:discard").waitForExistence(timeout: 10))
        app.buttons["Discard"].tap()
        XCTAssertTrue(element(app, "accounts:list").waitForExistence(timeout: 10))
        back(app)
        XCTAssertTrue(element(app, "drawer:manage").waitForExistence(timeout: 10))
        back(app)
        tap(app, "navigation:row:configuration:Demo_Build")
        tap(app, "history:build:1")
        XCTAssertTrue(waitForSharedContent(app, "details:actions", timeout: 20))
        XCTAssertTrue(element(app, "overview:row:Result").waitForExistence(timeout: 20))
        capture("history_single_details_visit", app)
        back(app)
        XCTAssertTrue(waitForSharedContent(app, "history:build:1", timeout: 10))
        XCTAssertFalse(element(app, "overview:screen").exists)
        capture("history_one_back", app)
    }

    func testRefreshedBuildSectionsUseTheLoadedSnapshot() throws {
        guard ProcessInfo.processInfo.environment["TEAMCITY_FIXTURE_UI"] == "1" else { throw XCTSkip("Read-only fixture is opt-in") }
        continueAfterFailure = false
        let app = fixtureApp("/refreshed")
        tap(app, "navigation:row:configuration:Demo_Build")
        XCTAssertTrue(waitForSharedContent(app, "history:build:1", timeout: 20))
        tap(app, "history:build:1")
        XCTAssertTrue(waitForSharedContent(app, "overview:section:Artifacts", timeout: 20))
        capture("refreshed_overview_sections", app)
        tap(app, "overview:section:Tests", scroll: true)
        XCTAssertTrue(element(app, "tests:test:failure").waitForExistence(timeout: 20))
        back(app)
        tap(app, "overview:section:Changes", scroll: true)
        XCTAssertTrue(element(app, "changes:change:5").waitForExistence(timeout: 20))
        back(app)
        tap(app, "overview:section:Parameters", scroll: true)
        XCTAssertTrue(app.staticTexts["env.PLATFORM"].waitForExistence(timeout: 20))
        XCTAssertFalse(app.staticTexts["stale"].exists)
        capture("refreshed_parameters", app)
        app.terminate(); app.launch()
        XCTAssertTrue(app.staticTexts["env.PLATFORM"].waitForExistence(timeout: 30))
        back(app)
        tap(app, "overview:section:Artifacts", scroll: true)
        XCTAssertTrue(app.staticTexts["report.txt"].firstMatch.waitForExistence(timeout: 20))
        capture("refreshed_artifacts", app)
        back(app); back(app)
        XCTAssertTrue(waitForSharedContent(app, "history:build:1", timeout: 15))
    }

    func testHomeRetainedPanesFavoritesSectionsAndRelaunch() throws {
        guard ProcessInfo.processInfo.environment["TEAMCITY_FIXTURE_UI"] == "1" else {
            throw XCTSkip("Start scripts/kmp-platform-fixture.py and opt in to fixture UI verification.")
        }
        continueAfterFailure = false
        let app = fixtureApp()
        capture("home_projects", app)
        tap(app, "home:tab:Favorites")
        acknowledgeHomePrompt(app, "Favorites")
        XCTAssertTrue(waitForSharedContent(app, "favorites:add", timeout: 20))
        capture("favorites_empty", app)
        tap(app, "home:tab:Activity")
        acknowledgeHomePrompt(app, "Running")
        tap(app, "home:filter:RunningAll")
        XCTAssertTrue(element(app, "running_builds:list").waitForExistence(timeout: 20))
        capture("running", app)
        tap(app, "home:tab:Queue")
        acknowledgeHomePrompt(app, "Queue")
        tap(app, "home:filter:QueuedAll")
        XCTAssertTrue(element(app, "build_queue:list").waitForExistence(timeout: 20))
        capture("queue", app)
        tap(app, "home:tab:Agents")
        acknowledgeHomePrompt(app, "Agents")
        XCTAssertTrue(waitForSharedContent(app, "agents:list", timeout: 20))
        capture("agents", app)
        tap(app, "home:filter:AgentsDisconnected")
        tap(app, "home:tab:Queue")
        tap(app, "home:tab:Projects")
        tap(app, "home:tab:Activity")
        XCTAssertTrue(element(app, "home:filter:QueuedAll").exists)
        XCTAssertTrue(element(app, "build_queue:list").exists)
        capture("retained_activity_queue", app)
        tap(app, "home:tab:Projects")
        tap(app, "navigation:row:configuration:Demo_Build")
        XCTAssertTrue(waitForSharedContent(app, "history:list", timeout: 20))
        tap(app, "history:favorite")
        XCTAssertTrue(app.buttons["Remove from favorites"].waitForExistence(timeout: 10))
        capture("history_favorite", app)
        tap(app, "history:build:1")
        XCTAssertTrue(waitForSharedContent(app, "overview:row:Result", timeout: 20))
        capture("overview_sections", app)
        tap(app, "overview:section:Changes", scroll: true)
        tap(app, "changes:change:5")
        XCTAssertTrue(element(app, "change_details:list").waitForExistence(timeout: 20))
        XCTAssertGreaterThanOrEqual(app.staticTexts["Change details"].firstMatch.frame.minY, 40)
        capture("change_details", app)
        back(app)
        XCTAssertTrue(element(app, "changes:list").waitForExistence(timeout: 20))
        capture("changes", app)
        back(app)
        tap(app, "overview:section:Tests", scroll: true)
        XCTAssertTrue(element(app, "tests:test:failure").waitForExistence(timeout: 20))
        capture("tests_failed", app)
        tap(app, "tests:test:failure")
        XCTAssertTrue(element(app, "test_details:text").waitForExistence(timeout: 20))
        capture("test_details", app)
        tap(app, "test_details:search")
        element(app, "test_details:search").typeText("Queue\n")
        capture("test_details_search", app)
        back(app)
        tap(app, "tests:filter:Passed")
        XCTAssertTrue(element(app, "tests:test:passed").waitForExistence(timeout: 20))
        capture("tests_passed", app)
        tap(app, "tests:filter:Ignored")
        XCTAssertTrue(app.staticTexts["There are no ignored tests"].waitForExistence(timeout: 20))
        capture("tests_empty_ignored", app)
        back(app)
        tap(app, "overview:section:Parameters", scroll: true)
        XCTAssertTrue(element(app, "properties:list").waitForExistence(timeout: 20))
        tap(app, "properties:expand:1", scroll: true)
        capture("parameters_expanded", app)
        app.terminate()
        app.launch()
        XCTAssertTrue(element(app, "properties:list").waitForExistence(timeout: 30))
        capture("section_relaunched", app)
        back(app)
        tap(app, "overview:section:Snapshots", scroll: true)
        XCTAssertTrue(element(app, "snapshot:list").waitForExistence(timeout: 20))
        capture("snapshot_dependencies", app)
        XCUIDevice.shared.press(.home)
        app.activate()
        XCTAssertTrue(element(app, "snapshot:list").waitForExistence(timeout: 20))
        capture("snapshot_after_background", app)
        tap(app, "home:tab:Favorites")
        XCTAssertTrue(element(app, "favorites:configuration:Demo_Build").waitForExistence(timeout: 20))
        capture("favorites_content", app)
        tap(app, "home:tab:Projects")
        XCTAssertTrue(element(app, "snapshot:list").waitForExistence(timeout: 20))
        XCUIDevice.shared.orientation = .landscapeLeft
        capture("retained_section_expanded", app)
        XCUIDevice.shared.orientation = .portrait
    }
    func testLoadingAndFailedMonitoringKeepHomeNavigationAvailable() throws {
        guard ProcessInfo.processInfo.environment["TEAMCITY_FIXTURE_UI"] == "1" else { throw XCTSkip("Fixture verification is opt-in") }
        continueAfterFailure = false
        let loading = fixtureApp("/loading")
        capture("home_loading", loading)
        XCTAssertTrue(element(loading, "navigation:row:configuration:Demo_Build").waitForExistence(timeout: 20))
        loading.terminate()
        let app = fixtureApp("/error")
        tap(app, "home:tab:Activity")
        tap(app, "home:filter:RunningAll")
        XCTAssertTrue(element(app, "error:content").waitForExistence(timeout: 20))
        capture("running_error", app)
        app.buttons["Try again"].firstMatch.tap()
        XCTAssertTrue(element(app, "error:content").waitForExistence(timeout: 20))
        tap(app, "home:tab:Queue")
        tap(app, "home:filter:QueuedAll")
        XCTAssertTrue(element(app, "error:content").waitForExistence(timeout: 20))
        capture("queue_error", app)
        tap(app, "home:tab:Agents")
        XCTAssertTrue(element(app, "error:content").waitForExistence(timeout: 20))
        capture("agents_error", app)
        tap(app, "home:tab:Projects")
        XCTAssertTrue(element(app, "navigation:row:configuration:Demo_Build").exists)
    }
    func testOptionalCountFailuresKeepChangesAndTestsUsable() throws {
        guard ProcessInfo.processInfo.environment["TEAMCITY_FIXTURE_UI"] == "1" else { throw XCTSkip("Fixture verification is opt-in") }
        continueAfterFailure = false
        let app = fixtureApp("/optional")
        tap(app, "navigation:row:configuration:Demo_Build")
        tap(app, "history:build:1")
        XCTAssertTrue(element(app, "overview:row:Result").waitForExistence(timeout: 20))
        capture("overview_optional_counts_unavailable", app)
        tap(app, "overview:section:Changes", scroll: true)
        XCTAssertTrue(element(app, "changes:change:5").waitForExistence(timeout: 20))
        capture("changes_optional_count_failure", app)
        back(app)
        tap(app, "overview:section:Tests", scroll: true)
        XCTAssertTrue(element(app, "tests:test:failure").waitForExistence(timeout: 20))
        capture("tests_optional_count_failure", app)
        tap(app, "tests:filter:Passed")
        XCTAssertTrue(element(app, "tests:test:passed").waitForExistence(timeout: 20))
    }

}

/// Mutations are limited to the explicitly enabled, local synthetic fixture.
final class PhaseFourManagementSmokeTests: XCTestCase {
    private func element(_ app: XCUIApplication, _ id: String) -> XCUIElement {
        app.descendants(matching: .any).matching(identifier: id).firstMatch
    }
    private func capture(_ name: String, _ app: XCUIApplication) {
        let bar = element(app, "screen:app-bar")
        let anchor = bar.exists ? bar : app
        var lastFrame = anchor.frame
        var unchangedSince = Date()
        let settled = NSPredicate { _, _ in
            let frame = anchor.frame
            if frame != lastFrame { lastFrame = frame; unchangedSince = Date() }
            return Date().timeIntervalSince(unchangedSince) >= 0.6
        }
        XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: settled, object: anchor)], timeout: 5), .completed)
        if bar.exists && XCUIDevice.shared.orientation == .portrait {
            assertNativeHeaderSafeArea(bar, app: app)
        }
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        let theme = ProcessInfo.processInfo.environment["TEAMCITY_UI_EVIDENCE_THEME"] ?? "light"
        attachment.name = "phase4_" + name + "_" + theme
        attachment.lifetime = .keepAlways
        add(attachment)
    }
    private func tap(_ app: XCUIApplication, _ id: String, scroll: Bool = false) {
        dismissSharedEducation(app)
        let target = element(app, id)
        for _ in 0..<20 {
            if target.exists { break }
            dismissSharedEducation(app)
            if target.waitForExistence(timeout: 0.5) { break }
        }
        if scroll {
            for _ in 0..<12 {
                if target.exists && target.isHittable { break }
                app.swipeUp(velocity: .slow)
            }
        }
        XCTAssertTrue(target.waitForExistence(timeout: 25), id)
        if scroll {
            var lastFrame = target.frame
            var unchangedSince = Date()
            let settled = NSPredicate { _, _ in
                let frame = target.frame
                if frame != lastFrame { lastFrame = frame; unchangedSince = Date() }
                return Date().timeIntervalSince(unchangedSince) >= 0.6
            }
            XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: settled, object: target)], timeout: 5), .completed)
        }
        target.press(forDuration: 0.2)
    }
    private func back(_ app: XCUIApplication) {
        let back = app.buttons["Navigate up"].exists ? app.buttons["Navigate up"].firstMatch : app.buttons["Back"].firstMatch
        XCTAssertTrue(back.waitForExistence(timeout: 10))
        back.press(forDuration: 0.2)
    }
    private var fixtureSuffix = ""
    private func fixtureApp(_ suffix: String = "") -> XCUIApplication {
        fixtureSuffix = suffix
        let app = XCUIApplication()
        app.launchEnvironment["TEAMCITY_UI_TEST_VAULT"] = UUID().uuidString
        app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        app.launch()
        prepareValidationLogin(app)
        tap(app, "auth:guest")
        let url = element(app, "auth:url")
        url.tap()
        for _ in 0..<8 { url.typeText(XCUIKeyboardKey.delete.rawValue) }
        for character in "http://127.0.0.1:18081/context" + suffix { url.typeText(String(character)) }
        XCTAssertEqual(url.value as? String, "http://127.0.0.1:18081/context" + suffix)
        url.typeText("\n")
        XCTAssertTrue(element(app, "auth:warning").waitForExistence(timeout: 10))
        app.buttons["OK"].tap()
        let homePrompt = element(app, "home:dismiss-prompt")
        _ = homePrompt.waitForExistence(timeout: 10)
        dismissSharedEducation(app)
        XCTAssertTrue(element(app, "home:tab:Favorites").waitForExistence(timeout: 30))
        return app
    }

    private func openHistory(_ app: XCUIApplication) {
        tap(app, "navigation:row:configuration:Demo_Build")
        XCTAssertTrue(waitForSharedContent(app, "history:build:1", timeout: 25))
    }
    private func localApp(_ mode: String = "manage") throws -> XCUIApplication {
        guard ProcessInfo.processInfo.environment["TEAMCITY_MANAGEMENT_UI"] == "1" else { throw XCTSkip("Synthetic management fixture is opt-in") }
        continueAfterFailure = false
        return fixtureApp("/" + mode + "/" + UUID().uuidString)
    }
    private func enterText(_ app: XCUIApplication, _ id: String, _ text: String) {
        let input = element(app, id)
        input.tap()
        let keyboard = app.keyboards.firstMatch
        XCTAssertTrue(keyboard.waitForExistence(timeout: 10))
        // Native focus/caret installation can outlive UIKit's app-idle signal.
        var previous = keyboard.frame
        var stableSince = Date()
        let stable = NSPredicate { _, _ in
            let frame = keyboard.frame
            if frame != previous { previous = frame; stableSince = Date() }
            return Date().timeIntervalSince(stableSince) >= 0.8
        }
        XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: stable, object: keyboard)], timeout: 10), .completed)
        var entered = ""
        for character in text {
            input.typeText(String(character))
            entered += String(character)
            XCTAssertEqual(input.value as? String, entered)
        }
    }
    private func assertRunRequest() {
        let completed = expectation(description: "Read the accepted synthetic Run request")
        let url = URL(string: "http://127.0.0.1:18081/context" + fixtureSuffix + "/guestAuth/app/rest/fixtureMutations")!
        URLSession.shared.dataTask(with: url) { data, response, error in
            defer { completed.fulfill() }
            XCTAssertNil(error)
            XCTAssertEqual((response as? HTTPURLResponse)?.statusCode, 200)
            guard let data = data, let document = try? JSONSerialization.jsonObject(with: data) as? [String: Any], let rows = document["mutations"] as? [[String: Any]] else { XCTFail("Missing synthetic mutation log"); return }
            XCTAssertEqual(rows.count, 1, "One submit must produce one accepted mutation")
            let body = rows.first?["body"] as? [String: Any]
            XCTAssertEqual(body?["personal"] as? Bool, true)
            let options = body?["triggeringOptions"] as? [String: Any]
            XCTAssertEqual(options?["queueAtTop"] as? Bool, true)
            XCTAssertEqual(options?["cleanSources"] as? Bool, false)
            XCTAssertEqual((body?["agent"] as? [String: Any])?["id"] as? String, "2")
            let parameters = (body?["properties"] as? [String: Any])?["property"] as? [[String: String]]
            XCTAssertEqual(parameters, [["name": "env.PHASE", "value": "four"]])
        }.resume()
        wait(for: [completed], timeout: 10)
    }
    func testRunOptionsParametersQueueShowAndRemove() throws {
        let app = try localApp()
        openHistory(app)
        capture("history_management_controls", app)
        tap(app, "history:run")
        XCTAssertTrue(element(app, "run-build:agent").waitForExistence(timeout: 20))
        capture("run_build", app)
        tap(app, "run-build:agent")
        capture("agent_chooser", app)
        tap(app, "run-build:agent:2")
        tap(app, "run-build:options", scroll: true)
        tap(app, "run-build:personal", scroll: true)
        tap(app, "run-build:top", scroll: true)
        tap(app, "run-build:clean", scroll: true)
        tap(app, "run-build:add", scroll: true)
        capture("parameter_dialog", app)
        tap(app, "parameter:confirm")
        capture("parameter_validation", app)
        enterText(app, "parameter:name", "env.PHASE")
        enterText(app, "parameter:value", "four")
        tap(app, "parameter:confirm")
        capture("run_custom_options", app)
        tap(app, "run-build:submit")
        XCTAssertTrue(element(app, "history:list").waitForExistence(timeout: 20))
        XCTAssertTrue(app.buttons["Show"].waitForExistence(timeout: 10))
        assertRunRequest()
        capture("queued_result", app)
        app.buttons["Show"].tap()
        XCTAssertTrue(element(app, "overview:row:WaitReason").waitForExistence(timeout: 20))
        XCTAssertTrue(element(app, "details:action:Cancel").waitForExistence(timeout: 10))
        capture("queued_overview", app)
        tap(app, "details:action:Cancel")
        capture("remove_confirmation", app)
        tap(app, "details:confirm")
        XCTAssertTrue(app.staticTexts["Build is removed from queue successfully"].waitForExistence(timeout: 20))
        capture("removed_success", app)
    }
    func testFiltersRestoreAndBranchActionSheet() throws {
        let app = try localApp()
        openHistory(app)
        tap(app, "history:filter")
        capture("filter_builds", app)
        tap(app, "filter-builds:status:Queued")
        capture("queued_filter", app)
        tap(app, "filter-builds:status:Failed")
        tap(app, "filter-builds:apply")
        XCTAssertTrue(element(app, "history:list").waitForExistence(timeout: 20))
        capture("filtered_history", app)
        app.terminate(); app.launch()
        XCTAssertTrue(element(app, "history:list").waitForExistence(timeout: 30))
        capture("filtered_history_relaunched", app)
        tap(app, "history:build:1")
        XCTAssertTrue(element(app, "details:action:Restart").waitForExistence(timeout: 20))
        capture("finished_overview", app)
        tap(app, "overview:row:Branch", scroll: true)
        XCTAssertTrue(element(app, "sheet:Branch").waitForExistence(timeout: 10))
        capture("branch_action_sheet", app)
        tap(app, "sheet:Branch")
        XCTAssertTrue(element(app, "history:list").waitForExistence(timeout: 20))
        capture("branch_filtered_history", app)
    }
    func testStopReaddConfirmationAndRestart() throws {
        let app = try localApp()
        tap(app, "home:tab:Activity")
        tap(app, "home:filter:RunningAll")
        tap(app, "running_builds:build:3")
        XCTAssertTrue(element(app, "details:action:Cancel").waitForExistence(timeout: 20))
        capture("running_build", app)
        tap(app, "details:action:Cancel")
        XCTAssertTrue(element(app, "details:readd").waitForExistence(timeout: 10))
        capture("stop_confirmation", app)
        app.buttons["Cancel"].tap()
        tap(app, "details:action:Cancel")
        tap(app, "details:readd")
        tap(app, "details:confirm")
        XCTAssertTrue(app.staticTexts["Build is stopped successfully"].waitForExistence(timeout: 20))
        capture("stopped_success", app)
        tap(app, "details:action:Restart")
        capture("restart_confirmation", app)
        tap(app, "details:confirm")
        XCTAssertTrue(app.buttons["Show"].waitForExistence(timeout: 20))
        capture("restarted_success", app)
        app.buttons["Show"].tap()
        XCTAssertTrue(element(app, "overview:row:WaitReason").waitForExistence(timeout: 20))
        capture("restarted_queued_overview", app)
    }
    func testForbiddenAndRetryStayOnRunForm() throws {
        let forbidden = try localApp("manage-forbidden")
        openHistory(forbidden); tap(forbidden, "history:run"); tap(forbidden, "run-build:submit")
        XCTAssertTrue(forbidden.staticTexts["You don't have permission to run this build."].waitForExistence(timeout: 20))
        capture("run_forbidden", forbidden)
        forbidden.terminate()
        let retry = try localApp("manage-retry")
        openHistory(retry); tap(retry, "history:run"); tap(retry, "run-build:submit")
        XCTAssertTrue(element(retry, "run-build:snackbar").waitForExistence(timeout: 20))
        capture("run_error", retry)
        tap(retry, "run-build:submit")
        XCTAssertTrue(retry.buttons["Show"].waitForExistence(timeout: 20))
        capture("run_retry_success", retry)
    }
}


/// System integrations use local, read-only fixtures and app-private files.
final class PhaseFivePlatformSmokeTests: XCTestCase {
    private func element(_ app: XCUIApplication, _ id: String) -> XCUIElement {
        app.descendants(matching: .any).matching(identifier: id).firstMatch
    }
    private func capture(_ name: String, _ app: XCUIApplication) {
        let bar = element(app, "screen:app-bar")
        let systemSheet = element(app, "ActivityListView")
        let anchor = systemSheet.exists ? systemSheet : (bar.exists ? bar : app)
        var lastFrame = anchor.frame
        var unchangedSince = Date()
        let settled = NSPredicate { _, _ in
            let frame = anchor.frame
            if frame != lastFrame { lastFrame = frame; unchangedSince = Date() }
            return Date().timeIntervalSince(unchangedSince) >= 0.6
        }
        XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: settled, object: anchor)], timeout: 10), .completed)
        if !systemSheet.exists && bar.exists && XCUIDevice.shared.orientation == .portrait {
            assertNativeHeaderSafeArea(bar, app: app)
        }
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        let theme = ProcessInfo.processInfo.environment["TEAMCITY_UI_EVIDENCE_THEME"] ?? "light"
        attachment.name = "phase5_" + name + "_" + theme
        attachment.lifetime = .keepAlways
        add(attachment)
    }
    private func tap(_ app: XCUIApplication, _ id: String, scroll: Bool = false) {
        dismissSharedEducation(app)
        let target = element(app, id)
        for _ in 0..<20 {
            if target.exists { break }
            dismissSharedEducation(app)
            if target.waitForExistence(timeout: 0.5) { break }
        }
        if scroll {
            for _ in 0..<12 {
                if target.exists && target.isHittable { break }
                app.swipeUp(velocity: .slow)
            }
        }
        XCTAssertTrue(target.waitForExistence(timeout: 25), id)
        if scroll {
            var lastFrame = target.frame
            var unchangedSince = Date()
            let settled = NSPredicate { _, _ in
                let frame = target.frame
                if frame != lastFrame { lastFrame = frame; unchangedSince = Date() }
                return Date().timeIntervalSince(unchangedSince) >= 0.6
            }
            XCTAssertEqual(XCTWaiter.wait(for: [XCTNSPredicateExpectation(predicate: settled, object: target)], timeout: 5), .completed)
        }
        target.press(forDuration: 0.2)
    }
    private func back(_ app: XCUIApplication) {
        let back = app.buttons["Navigate up"].exists ? app.buttons["Navigate up"].firstMatch : app.buttons["Back"].firstMatch
        XCTAssertTrue(back.waitForExistence(timeout: 10))
        back.press(forDuration: 0.2)
    }
    private var fixtureSuffix = ""
    private func fixtureApp(_ suffix: String = "") -> XCUIApplication {
        fixtureSuffix = suffix
        let app = XCUIApplication()
        app.launchEnvironment["TEAMCITY_UI_TEST_VAULT"] = UUID().uuidString
        app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        app.launch()
        prepareValidationLogin(app)
        tap(app, "auth:guest")
        let url = element(app, "auth:url")
        url.tap()
        for _ in 0..<8 { url.typeText(XCUIKeyboardKey.delete.rawValue) }
        for character in teamCityReadOnlyFixtureBaseURL + suffix { url.typeText(String(character)) }
        XCTAssertEqual(url.value as? String, teamCityReadOnlyFixtureBaseURL + suffix)
        url.typeText("\n")
        XCTAssertTrue(element(app, "auth:warning").waitForExistence(timeout: 10))
        app.buttons["OK"].tap()
        approveReadOnlyFixtureNetworkIfPresented()
        let homePrompt = element(app, "home:dismiss-prompt")
        _ = homePrompt.waitForExistence(timeout: 10)
        dismissSharedEducation(app)
        XCTAssertTrue(element(app, "home:tab:Favorites").waitForExistence(timeout: 30))
        return app
    }

    private func openHistory(_ app: XCUIApplication) {
        tap(app, "navigation:row:configuration:Demo_Build")
        XCTAssertTrue(waitForSharedContent(app, "history:build:1", timeout: 25))
    }

    private func platformApp(_ mode: String = "platform") throws -> XCUIApplication {
        guard ProcessInfo.processInfo.environment["TEAMCITY_PLATFORM_UI"] == "1" else { throw XCTSkip("Platform fixture is opt-in") }
        continueAfterFailure = false
        return fixtureApp("/" + mode + "/" + UUID().uuidString)
    }
    private func artifacts(_ app: XCUIApplication) {
        openHistory(app)
        tap(app, "history:build:1")
        capture("build_overview_platform_actions", app)
        tap(app, "overview:section:Artifacts", scroll: true)
        XCTAssertTrue(app.staticTexts["report.txt"].firstMatch.waitForExistence(timeout: 20))
    }
    private func waitUntilGone(_ app: XCUIApplication, _ id: String) {
        let gone = XCTNSPredicateExpectation(predicate: NSPredicate(format: "exists == false"), object: element(app, id))
        XCTAssertEqual(XCTWaiter.wait(for: [gone], timeout: 10), .completed, id)
    }
    private func assertFileExport(_ app: XCUIApplication, timeout: TimeInterval = 25) {
        let sheet = element(app, "ActivityListView")
        XCTAssertTrue(sheet.waitForExistence(timeout: timeout))
        let save = element(app, "Save to Files")
        // At accessibility sizes iOS collapses file actions behind this native disclosure.
        let more = element(app, "View More")
        if !save.exists && more.waitForExistence(timeout: 2) { more.tap() }
        for _ in 0..<6 {
            if save.exists { break }
            sheet.swipeUp(velocity: .slow)
        }
        XCTAssertTrue(save.waitForExistence(timeout: 10), "The native activity sheet must offer Save to Files")
    }
    private func download(_ app: XCUIApplication) {
        app.staticTexts["report.txt"].firstMatch.tap()
        tap(app, "sheet:ArtifactDownload")
    }
    func testArtifactFoldersArchivesAndNativeExport() throws {
        let app = try platformApp()
        artifacts(app)
        capture("artifacts", app)
        app.staticTexts["logs"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["report.txt"].firstMatch.waitForExistence(timeout: 15))
        capture("artifact_folder", app)
        back(app)
        app.staticTexts["bundle.zip"].firstMatch.tap()
        capture("archive_actions", app)
        tap(app, "sheet:ArtifactOpen")
        XCTAssertTrue(app.staticTexts["report.txt"].firstMatch.waitForExistence(timeout: 15))
        capture("archive_folder", app)
        back(app)
        download(app)
        capture("file_export_initial", app)
        assertFileExport(app)
        capture("file_export", app)
        // Cancelling the native sheet must leave a reusable completed file, not redownload it.
        let sheet = element(app, "ActivityListView")
        sheet.swipeDown(velocity: .slow)
        XCTAssertTrue(element(app, "artifacts:open-downloaded").waitForExistence(timeout: 10))
        capture("download_complete_reexport", app)
        if #available(iOS 17.0, *) { try app.performAccessibilityAudit() }
        tap(app, "artifacts:open-downloaded")
        assertFileExport(app)
        capture("file_reexport", app)
    }
    func testDownloadCancelBackgroundAndDeparture() throws {
        let app = try platformApp("artifact-slow")
        artifacts(app)
        download(app)
        XCTAssertTrue(element(app, "artifacts:download-card").waitForExistence(timeout: 10))
        capture("download_progress", app)
        app.buttons["Cancel"].firstMatch.tap()
        waitUntilGone(app, "artifacts:download-card")
        download(app)
        XCTAssertTrue(element(app, "artifacts:download-card").waitForExistence(timeout: 10))
        XCUIDevice.shared.press(.home)
        XCTAssertTrue(app.wait(for: .runningBackground, timeout: 10))
        app.activate()
        waitUntilGone(app, "artifacts:download-card")
        capture("download_after_background", app)
        download(app)
        XCTAssertTrue(element(app, "artifacts:download-card").waitForExistence(timeout: 10))
        back(app)
        XCTAssertTrue(element(app, "overview:section:Artifacts").waitForExistence(timeout: 10))
        tap(app, "overview:section:Artifacts")
        waitUntilGone(app, "artifacts:download-card")
        capture("download_after_departure", app)
    }
    func testDownloadFailureRetriesTheSameFile() throws {
        let app = try platformApp("artifact-retry")
        artifacts(app); download(app)
        XCTAssertTrue(app.buttons["Retry download"].firstMatch.waitForExistence(timeout: 20))
        capture("download_failure", app)
        app.buttons["Retry download"].firstMatch.tap()
        assertFileExport(app)
        capture("download_retry_export", app)
    }
    func testBrowserSharingAndClipboard() throws {
        let app = try platformApp()
        openHistory(app); tap(app, "history:build:1")
        tap(app, "details:action:Share")
        XCTAssertTrue(element(app, "ActivityListView").waitForExistence(timeout: 15))
        capture("build_share", app)
        // Dismiss UIKit's sheet through its standard close control.
        if app.buttons["Close"].firstMatch.exists { app.buttons["Close"].firstMatch.tap() }
        else { app.swipeDown(velocity: .slow) }
        tap(app, "overview:section:Parameters", scroll: true)
        tap(app, "properties:row:0")
        capture("parameter_copy", app)
        back(app)
        tap(app, "details:action:Browser")
        XCTAssertTrue(app.wait(for: .runningBackground, timeout: 15))
        capture("build_browser", app)
        app.activate()
        XCTAssertTrue(element(app, "details:action:Browser").waitForExistence(timeout: 15))
    }

    func testLongTransferSurvivesJsonDeadlineAndExports() throws {
        let app = try platformApp("artifact-long")
        artifacts(app); download(app)
        XCTAssertTrue(element(app, "artifacts:download-card").waitForExistence(timeout: 10))
        capture("long_download_progress", app)
        assertFileExport(app, timeout: 70)
        capture("long_download_export", app)
    }

    func testSettingsThemePersistenceAndAboutLicenses() throws {
        let app = try platformApp()
        tap(app, "home:account")
        capture("accounts_platform_actions", app)
        tap(app, "drawer:settings")
        capture("settings", app)
        tap(app, "settings:option:Dark")
        capture("settings_dark", app)
        app.terminate(); app.launch()
        XCTAssertTrue(element(app, "settings:option:Dark").waitForExistence(timeout: 25))
        capture("settings_dark_relaunched", app)
        tap(app, "settings:option:System")
        back(app)
        tap(app, "drawer:about")
        XCTAssertTrue(app.staticTexts["Version"].firstMatch.waitForExistence(timeout: 20))
        capture("about", app)
        tap(app, "Rate the app", scroll: true)
        let dismissRating = app.buttons["Not Now"].firstMatch
        if dismissRating.waitForExistence(timeout: 8) {
            capture("rating_prompt", app)
            dismissRating.tap()
            waitUntilGone(app, "Not Now")
        }
        capture("rating_returned", app)
        tap(app, "Open sources libraries", scroll: true)
        XCTAssertTrue(element(app, "licenses:list").waitForExistence(timeout: 10))
        capture("licenses", app)
        tap(app, "licenses:kotlin")
        XCTAssertTrue(element(app, "license:text").waitForExistence(timeout: 10))
        capture("license_offline_text", app)
    }
}
