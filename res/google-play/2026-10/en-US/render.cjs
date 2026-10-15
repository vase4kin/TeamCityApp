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

// Native vector layouts retain real app captures; no generated UI is used.
const fs = require('node:fs/promises');
const path = require('node:path');
const sharp = require('sharp');
const root = __dirname;
const copyright = 'Copyright 2026 Andrey Tolpeev';
const license = `Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.`;
const assets = [
  { id: '01-projects', source: 'projects', theme: 'light', lines: ['TeamCity,', 'in your pocket.'], alt: 'TeamCityApp in light theme showing projects and build configurations on a connected TeamCity server.' },
  { id: '02-build-overview', source: 'overview', theme: 'dark', lines: ['Every build.', 'All the details.'], alt: 'A successful build in dark theme with its status, log link, changes, artifacts, and execution details.' },
  { id: '03-build-history', source: 'history', theme: 'light', lines: ['Follow your', 'build history.'], alt: 'Build history in light theme showing successful builds, branches, test summaries, and build actions.' },
  { id: '04-running-builds', source: 'running', theme: 'dark', lines: ['See what’s', 'running.'], alt: 'Activity in dark theme showing running builds, status summaries, branches, and Running, Queue, and Agents tabs.' },
  { id: '05-test-results', source: 'tests', theme: 'light', lines: ['Inspect your', 'test results.'], alt: 'Build test results in light theme with Failed, Passed, and Ignored filters and individual test results.' },
  { id: '06-build-artifacts', source: 'artifacts', theme: 'dark', lines: ['Find your', 'build artifacts.'], alt: 'Build artifacts in dark theme showing a resource folder and files with their sizes.' },
  { id: '07-build-agents', source: 'agents', theme: 'light', lines: ['Keep track of', 'your agents.'], alt: 'Activity in light theme showing TeamCity build agents and Connected and Disconnected filters.' },
  { id: '08-run-build', source: 'run-build', theme: 'dark', lines: ['Run builds.', 'Your way.'], alt: 'The Run build form in dark theme with branch and agent selection, run options, and custom parameters.' },
];

const escape = (text) => text.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
const openSvg = (width, height, title) => `<?xml version="1.0" encoding="UTF-8"?>
<!-- ${copyright}\n\n${license} -->
<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" role="img">
<title>${escape(title)}</title>`;

function phoneSvg(asset, capture) {
  return `${openSvg(1080, 1920, asset.alt)}
  <defs>
    <linearGradient id="blue" x1="0" y1="0" x2="0.8" y2="1">
      <stop stop-color="#edf4ff"/><stop offset="1" stop-color="#c6dbff"/>
    </linearGradient>
    <clipPath id="screen"><rect x="80" y="324" width="920" height="1473.704" rx="28"/></clipPath>
  </defs>
  <rect width="1080" height="1920" fill="url(#blue)"/>
  <circle cx="1110" cy="1360" r="750" fill="none" stroke="#a9c8fa" stroke-width="2" opacity="0.5"/>
  <circle cx="1110" cy="1360" r="830" fill="none" stroke="#a9c8fa" stroke-width="2" opacity="0.32"/>
  <g font-family="Arial, Helvetica, sans-serif">
    <text x="76" y="170" fill="#102e5a" font-size="80" font-weight="700" letter-spacing="-2.8">${escape(asset.lines[0])}</text>
    <text x="76" y="254" fill="#102e5a" font-size="80" font-weight="700" letter-spacing="-2.8">${escape(asset.lines[1])}</text>
  </g>
  <rect x="74" y="336" width="932" height="1480" rx="34" fill="#5584ca" opacity="0.10"/>
  <rect x="76" y="320" width="928" height="1481.704" rx="32" fill="#ffffff" opacity="0.7"/>
  <g clip-path="url(#screen)">
    <!-- Only Android system bars are cropped. App content keeps its original proportions. -->
    <svg x="80" y="324" width="920" height="1473.704" viewBox="0 130 1080 1730">
      <image width="1080" height="1920" href="data:image/png;base64,${capture}"/>
    </svg>
  </g>
</svg>
`;
}

function featureSvg() {
  return `${openSvg(1024, 500, 'TeamCityApp. TeamCity in your pocket. Builds, tests, and artifacts wherever you are.')}
  <defs><linearGradient id="blue" x2="1" y2="1"><stop stop-color="#1858b8"/><stop offset="1" stop-color="#0d357c"/></linearGradient></defs>
  <rect width="1024" height="500" fill="url(#blue)"/>
  <circle cx="880" cy="310" r="250" fill="none" stroke="#8bb7ff" stroke-width="2" opacity="0.3"/>
  <circle cx="880" cy="310" r="320" fill="none" stroke="#8bb7ff" stroke-width="2" opacity="0.2"/>
  <g font-family="Arial, Helvetica, sans-serif">
    <text x="128" y="135" fill="#cadfff" font-size="27" font-weight="700">TeamCityApp</text>
    <text x="125" y="225" fill="#ffffff" font-size="62" font-weight="700" letter-spacing="-2">TeamCity in</text>
    <text x="125" y="292" fill="#ffffff" font-size="62" font-weight="700" letter-spacing="-2">your pocket.</text>
    <text x="128" y="365" fill="#cadfff" font-size="26">Builds. Tests. Artifacts.</text>
  </g>
  <g transform="translate(792 244)">
    <circle r="55" fill="#a9efbf"/>
    <path d="M-23 0 -6 17 25 -18" fill="none" stroke="#0d4b2b" stroke-width="9" stroke-linecap="round" stroke-linejoin="round"/>
  </g>
</svg>
`;
}

async function render(svg, file, width, height) {
  await sharp(Buffer.from(svg)).flatten({ background: '#ffffff' }).removeAlpha().png({ compressionLevel: 9 }).toFile(file);
  const metadata = await sharp(file).metadata();
  if (metadata.width !== width || metadata.height !== height || metadata.hasAlpha || metadata.channels !== 3) {
    throw new Error(`Invalid dimensions or color format: ${file}`);
  }
  if ((await fs.stat(file)).size > 8 * 1024 * 1024) throw new Error(`Oversized file: ${file}`);
}

async function main() {
  await fs.mkdir(path.join(root, 'editable'), { recursive: true });
  await fs.mkdir(path.join(root, 'phone'), { recursive: true });
  for (const asset of assets) {
    const captureFile = path.join(root, 'source', `${asset.source}.png`);
    const metadata = await sharp(captureFile).metadata();
    if (metadata.width !== 1080 || metadata.height !== 1920) throw new Error(`Unexpected source size: ${captureFile}`);
    const svg = phoneSvg(asset, (await fs.readFile(captureFile)).toString('base64'));
    await fs.writeFile(path.join(root, 'editable', `${asset.id}.svg`), svg);
    await render(svg, path.join(root, 'phone', `${asset.id}.png`), 1080, 1920);
  }
  const feature = featureSvg();
  await fs.writeFile(path.join(root, 'editable', 'feature-graphic.svg'), feature);
  await render(feature, path.join(root, 'feature-graphic.png'), 1024, 500);

  // Preview only: the individually numbered PNGs are the upload assets.
  const previews = await Promise.all(assets.map((asset) => sharp(path.join(root, 'phone', `${asset.id}.png`)).resize(270, 480).toBuffer()));
  await sharp({ create: { width: 1152, height: 1032, channels: 3, background: '#e7f0ff' } })
    .composite(previews.map((input, i) => ({ input, left: 24 + (i % 4) * 278, top: 24 + Math.floor(i / 4) * 504 })))
    .png().toFile(path.join(root, 'preview.png'));
  await fs.writeFile(path.join(root, 'manifest.json'), JSON.stringify({
    copyright, license, locale: 'en-US', phoneDimensions: [1080, 1920], format: '24-bit RGB PNG, no alpha',
    listing: 'https://play.google.com/store/apps/details?id=com.github.vase4kin.teamcityapp',
    requirements: 'https://support.google.com/googleplay/android-developer/answer/9866151?hl=en',
    captures: 'Current app on Android emulator; public guest data for light screens and running builds; bundled mock data for overview, artifacts, and build setup.',
    assets: assets.map((asset) => ({ file: `phone/${asset.id}.png`, title: asset.lines.join(' '), theme: asset.theme, altText: asset.alt, source: `source/${asset.source}.png` })),
    featureGraphic: { file: 'feature-graphic.png', dimensions: [1024, 500], altText: 'TeamCityApp: TeamCity in your pocket. Builds, tests, and artifacts wherever you are.' },
  }, null, 2) + '\n');
  const gallery = path.resolve(root, '../../../screenshots');
  await fs.mkdir(gallery, { recursive: true });
  for (const [index, asset] of assets.entries()) {
    await fs.copyFile(path.join(root, 'phone', `${asset.id}.png`), path.join(gallery, `Phone Screenshot ${index + 1}.png`));
  }
  await fs.copyFile(path.join(root, 'feature-graphic.png'), path.join(gallery, 'feature-graphic.png'));
  console.log('Rendered and validated eight 1080×1920 RGB screenshots and one 1024×500 feature graphic; updated the repository gallery.');
}

main().catch((error) => { console.error(error); process.exitCode = 1; });
