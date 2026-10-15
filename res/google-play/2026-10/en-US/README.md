<!--
Copyright 2026 Andrey Tolpeev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# TeamCityApp Google Play assets

English phone listing set, prepared on 14 October 2026. The eight screenshots
use a consistent blue layout and alternate between four light and four dark
app screens. Matching PNGs in `res/screenshots` supply the repository README gallery.
Each phone asset shows only its large headline above the app screenshot, without
a small brand label or visible screenshot numbering.

## Upload files

Upload the eight PNGs in `phone/` in filename order to the English listing's
**Phone screenshots** section. Each is 1080 × 1920, 9:16, 24-bit RGB PNG without
transparency. Upload `feature-graphic.png` separately as the **Feature graphic**;
it is 1024 × 500, also RGB PNG without transparency.

`manifest.json` contains the upload order, headlines, themes, source paths, and
alt text for every asset. `preview.png` is a contact sheet for review and should
not be uploaded as a phone screenshot. These files have not been published.

The export dimensions and formats follow [Google Play's preview asset requirements](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).
The layout keeps captions above the app content, with no device hardware or
store badges. Headlines occupy less than 20% of the canvas.

## Capture provenance

`source/` contains original, unretouched Android emulator captures at 1080 × 1920
and 400 dpi, taken from the current installed application:

- Projects, History, Tests, Running, and Agents show a public guest account on
  `https://teamcity.jetbrains.com` in the production debug flavor.
- Overview, Artifacts, and Run build show the app's existing bundled mock
  fixtures in the mock debug flavor. Fixture names and their 2016 build dates
  are part of that demo data.

No app UI or build data was synthesized. The SVG layouts crop only the Android
status and gesture bars, preserve app content proportions, and add the blue
background, headline, and subtle border. Emulator display overrides and the
production app's original **Follow system** theme were restored after capture.
No build was submitted, stopped, or restarted.

## Edit and regenerate

Each file in `editable/` is a self-contained SVG with its real screenshot
embedded. Edit a headline, colors, or layout in a vector editor, or update the
asset definitions in `render.cjs` and regenerate all outputs, including the
repository gallery in `res/screenshots`:

```sh
node res/google-play/2026-10/en-US/render.cjs
```

Rendering requires Node.js and the `sharp` package, using an installed Arial
or Helvetica font. In Codex, the bundled Node runtime and packages can be used
without adding dependencies to the Android application:

```sh
NODE_PATH=/Users/vase4kin/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules \
  /Users/vase4kin/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node \
  res/google-play/2026-10/en-US/render.cjs
```

The renderer validates source dimensions and every final asset's dimensions,
RGB channels, lack of alpha, and file size. Final PNGs and source captures are
intentional listing assets. The copyright and Apache 2.0 notice above applies
to this asset package, including its binary PNG files.
