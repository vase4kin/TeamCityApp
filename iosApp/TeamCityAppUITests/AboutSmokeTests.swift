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

final class AboutSmokeTests: XCTestCase {
    private func launch(state: String = "available", theme: String = "light") -> XCUIApplication {
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchEnvironment["PHASE1_STATE"] = state
        app.launchEnvironment["PHASE1_THEME"] = theme
        app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        app.launch()
        return app
    }

    private func capture(_ name: String, app: XCUIApplication) {
        let image = XCTAttachment(screenshot: app.screenshot())
        image.name = name
        image.lifetime = .keepAlways
        add(image)
    }

    func testAvailableLight() {
        let app = launch()
        XCTAssertTrue(app.staticTexts["https://teamcity.example"].waitForExistence(timeout: 20))
        XCTAssertTrue(app.staticTexts["2026.1"].exists)
        capture("about_available_light", app: app)
        app.swipeUp()
        XCTAssertTrue(app.staticTexts["Privacy policy"].waitForExistence(timeout: 5))
        capture("about_available_light_bottom", app: app)
    }

    func testAvailableDark() {
        let app = launch(theme: "dark")
        XCTAssertTrue(app.staticTexts["https://teamcity.example"].waitForExistence(timeout: 20))
        capture("about_available_dark", app: app)
    }

    func testUnavailableLight() {
        let app = launch(state: "unavailable")
        // Live-region text is an accessibility element on iOS, not always a StaticText.
        let notice = app.descendants(matching: .any).matching(
            NSPredicate(format: "label == %@", "Server information is unavailable")
        ).firstMatch
        XCTAssertTrue(notice.waitForExistence(timeout: 20))
        XCTAssertTrue(app.staticTexts["Application details"].exists)
        capture("about_unavailable_light", app: app)
    }

    func testUnavailableDark() {
        let app = launch(state: "unavailable", theme: "dark")
        let notice = app.descendants(matching: .any).matching(
            NSPredicate(format: "label == %@", "Server information is unavailable")
        ).firstMatch
        XCTAssertTrue(notice.waitForExistence(timeout: 20))
        capture("about_unavailable_dark", app: app)
    }

    func testLoading() {
        let app = launch(state: "loading")
        XCTAssertTrue(app.otherElements["screen:app-bar"].waitForExistence(timeout: 20))
        XCTAssertFalse(app.staticTexts["Server info"].exists)
        capture("about_loading_light", app: app)
    }

    func testLoadingDark() {
        let app = launch(state: "loading", theme: "dark")
        XCTAssertTrue(app.otherElements["screen:app-bar"].waitForExistence(timeout: 20))
        XCTAssertFalse(app.staticTexts["Server info"].exists)
        capture("about_loading_dark", app: app)
    }

    func testPreviewActionReportsUnavailable() {
        let app = launch()
        let rate = app.staticTexts["Rate the app"]
        XCTAssertTrue(rate.waitForExistence(timeout: 20))
        rate.tap()
        XCTAssertTrue(app.staticTexts["This action is not available in this preview."].waitForExistence(timeout: 5))
    }
}
