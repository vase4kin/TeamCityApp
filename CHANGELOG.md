# Changelog

## [2.0.0-beta.1](https://github.com/vase4kin/TeamCityApp/compare/v2.0.0-beta.0...v2.0.0-beta.1) (2026-10-13)


### Features

* **build-history:** show year for dates outside the current year ([#454](https://github.com/vase4kin/TeamCityApp/issues/454)) ([29d9c2c](https://github.com/vase4kin/TeamCityApp/commit/29d9c2c7f8351199a968287af6008604d621edbe))
* **build:** migrate Android compilation and resource optimization ([#449](https://github.com/vase4kin/TeamCityApp/issues/449)) ([436ad8c](https://github.com/vase4kin/TeamCityApp/commit/436ad8c0a806857c05ad3a1710c6ee08883675b9))
* **home:** add compact Activity filter chips ([#453](https://github.com/vase4kin/TeamCityApp/issues/453)) ([681bc61](https://github.com/vase4kin/TeamCityApp/commit/681bc610f6ba02f71764131f1b1a533f696ca8d6))
* **home:** remove Activity navigation counter ([2a4c8f1](https://github.com/vase4kin/TeamCityApp/commit/2a4c8f19564358ce68e6cb3ee65d2f281e834eed))
* **home:** show agents counts in activity navigation ([#446](https://github.com/vase4kin/TeamCityApp/issues/446)) ([87fa7a5](https://github.com/vase4kin/TeamCityApp/commit/87fa7a51dda373502ad2776fbb3acb02c2fd7f64))
* **ui:** keep operation progress in screens ([#455](https://github.com/vase4kin/TeamCityApp/issues/455)) ([8566396](https://github.com/vase4kin/TeamCityApp/commit/8566396319233cc7e15254443be21495f686dc70))


### Bug Fixes

* **a11y:** add hints and accurate action semantics ([#457](https://github.com/vase4kin/TeamCityApp/issues/457)) ([659810f](https://github.com/vase4kin/TeamCityApp/commit/659810f8adabdd6021b74607b30b5444c4493308))
* **changes:** improve comment previews and detail action ([#450](https://github.com/vase4kin/TeamCityApp/issues/450)) ([9e4869e](https://github.com/vase4kin/TeamCityApp/commit/9e4869ea4e234560bd284e89b34a4d93e8cbea3f))
* **history:** render cached builds during back transitions ([#459](https://github.com/vase4kin/TeamCityApp/issues/459)) ([d30e0b4](https://github.com/vase4kin/TeamCityApp/commit/d30e0b4fc31046447a1ea504af9904f17ec9c7fa))
* **home:** remove Activity filter confirmation snackbars ([#458](https://github.com/vase4kin/TeamCityApp/issues/458)) ([46b8c01](https://github.com/vase4kin/TeamCityApp/commit/46b8c0137e3e40dde24de58598a43bcdec84dfaa))
* **run-build:** execute network requests on the IO dispatcher ([#452](https://github.com/vase4kin/TeamCityApp/issues/452)) ([cac6cc3](https://github.com/vase4kin/TeamCityApp/commit/cac6cc31dbab79b7a0d7a6d48f223dd6abf8e6e2))
* **run-build:** show submission failures in a snackbar ([#448](https://github.com/vase4kin/TeamCityApp/issues/448)) ([de71ac6](https://github.com/vase4kin/TeamCityApp/commit/de71ac6b5d393cebc75010c69d2a44d38049b200))
* **splash:** use a single Android launch splash ([#460](https://github.com/vase4kin/TeamCityApp/issues/460)) ([c43bff0](https://github.com/vase4kin/TeamCityApp/commit/c43bff0e429079fdcd1fd8d1fd95dacbf49da1fc))
* **ui:** keep floating build actions fixed on the right ([#456](https://github.com/vase4kin/TeamCityApp/issues/456)) ([0cd3eef](https://github.com/vase4kin/TeamCityApp/commit/0cd3eefb16503bf948fc09dd8a598dbb8a14ef63))

## [2.0.0-beta.0](https://github.com/vase4kin/TeamCityApp/compare/v1.52.8...v2.0.0-beta.0) (2026-10-13)


### Features

* **about:** migrate screen to Compose and lifecycle-aware state ([#393](https://github.com/vase4kin/TeamCityApp/issues/393)) ([37a977a](https://github.com/vase4kin/TeamCityApp/commit/37a977ac506d8c9a0a555a03d87f5e995e52f257))
* **accounts:** migrate account creation to Compose ([faf4a51](https://github.com/vase4kin/TeamCityApp/commit/faf4a51b29c775e7d31e74ef7cc339d768423257))
* **build-details:** add grouped overview with floating actions ([#431](https://github.com/vase4kin/TeamCityApp/issues/431)) ([b864dd1](https://github.com/vase4kin/TeamCityApp/commit/b864dd12c8eef158c8ef67a2e0f71b18827d5468))
* **builds:** migrate run build to Compose ([7bfd070](https://github.com/vase4kin/TeamCityApp/commit/7bfd0706702adf8d2e2c7ab5c553ad87f3b5bc53))
* **change-details:** migrate change details to Compose ([#402](https://github.com/vase4kin/TeamCityApp/issues/402)) ([6259d5f](https://github.com/vase4kin/TeamCityApp/commit/6259d5f32cb13502bec7dc21f77b49590f647da0))
* **ci:** automate releases with Release Please ([#387](https://github.com/vase4kin/TeamCityApp/issues/387)) ([177904e](https://github.com/vase4kin/TeamCityApp/commit/177904e11cf8c7bd733968fd6d335141d2d3b1cb))
* **drawer:** migrate account drawer to Compose ([#405](https://github.com/vase4kin/TeamCityApp/issues/405)) ([6ddf164](https://github.com/vase4kin/TeamCityApp/commit/6ddf164ca473b37040fed1bcb372c53d856c9c2d))
* **filters:** migrate build filters to Compose ([46178a3](https://github.com/vase4kin/TeamCityApp/commit/46178a3f8ff8cc04d75da70edf030b858938cc91))
* **filters:** migrate quick filter sheet to Compose ([6a7877f](https://github.com/vase4kin/TeamCityApp/commit/6a7877fcb5cd6d06fa414aad3812904bfacf1be1))
* **history:** add collapsible floating toolbar ([#429](https://github.com/vase4kin/TeamCityApp/issues/429)) ([1365263](https://github.com/vase4kin/TeamCityApp/commit/136526360100fa46fc5b48c684530b7c6b7cfd69))
* **home:** simplify navigation and surface activity updates ([#438](https://github.com/vase4kin/TeamCityApp/issues/438)) ([62e393b](https://github.com/vase4kin/TeamCityApp/commit/62e393bad58e6ffe669c5d705c7d9c6bddec75d3))
* **login:** migrate login to Compose ([418cbfc](https://github.com/vase4kin/TeamCityApp/commit/418cbfc9b8c4ba18a7c15c4dcd00453e6c69fbf5))
* **logs:** migrate build log to Compose ([2cbba7c](https://github.com/vase4kin/TeamCityApp/commit/2cbba7c6b89704bf80160e3a1a95ecfca7d880e5))
* **manage-accounts:** migrate account management to Compose ([#404](https://github.com/vase4kin/TeamCityApp/issues/404)) ([8cac837](https://github.com/vase4kin/TeamCityApp/commit/8cac837b162cd58f07169b36d9af20c2ec6d6347))
* **navigation:** migrate to single-activity Compose navigation ([#428](https://github.com/vase4kin/TeamCityApp/issues/428)) ([be191fc](https://github.com/vase4kin/TeamCityApp/commit/be191fc9f9e1674923e5967b3a2dfeecfdb39454))
* **navigation:** retain Home navigation across tab flows ([#440](https://github.com/vase4kin/TeamCityApp/issues/440)) ([ae44e66](https://github.com/vase4kin/TeamCityApp/commit/ae44e66fe231dc2722fc12940ce12fc6e8e74c0b))
* **properties:** migrate parameter list to Compose ([#400](https://github.com/vase4kin/TeamCityApp/issues/400)) ([55c6316](https://github.com/vase4kin/TeamCityApp/commit/55c63166f06e93da0a2d11b529ec528e308a08bb))
* **settings:** migrate theme preferences to Compose ([#403](https://github.com/vase4kin/TeamCityApp/issues/403)) ([758aa4f](https://github.com/vase4kin/TeamCityApp/commit/758aa4f50c9a07c0444501b671f0ff43ea37744e))
* **splash:** migrate startup screen to Compose ([#406](https://github.com/vase4kin/TeamCityApp/issues/406)) ([e14be9a](https://github.com/vase4kin/TeamCityApp/commit/e14be9ae65e104262df42286eb59938abcbbeda9))
* **test-details:** migrate test details to Compose ([#401](https://github.com/vase4kin/TeamCityApp/issues/401)) ([c3c58bb](https://github.com/vase4kin/TeamCityApp/commit/c3c58bba24ce80ba1b9936e89b202366266cd235))
* **ui:** migrate action bottom sheet to Compose ([842744c](https://github.com/vase4kin/TeamCityApp/commit/842744ce6ff6260f62d224ee2929b2a4f61e7c4f))
* **ui:** migrate remaining lists to Compose ([#425](https://github.com/vase4kin/TeamCityApp/issues/425)) ([a9d99c0](https://github.com/vase4kin/TeamCityApp/commit/a9d99c0f44eb223afb15d93e1422105e2924f9f1))
* **ui:** redesign migrated screens with Material 3 Expressive ([#424](https://github.com/vase4kin/TeamCityApp/issues/424)) ([b35645a](https://github.com/vase4kin/TeamCityApp/commit/b35645a5c03b5bbe2bdaf4f0ed74c5bdca3999ba))


### Bug Fixes

* **api:** defer account graph creation until first use ([#394](https://github.com/vase4kin/TeamCityApp/issues/394)) ([0dc3d2c](https://github.com/vase4kin/TeamCityApp/commit/0dc3d2c808e762b465ca59cd2b0721c59065b210))
* **build-details:** match action FAB height to floating toolbar ([#439](https://github.com/vase4kin/TeamCityApp/issues/439)) ([ff7b840](https://github.com/vase4kin/TeamCityApp/commit/ff7b840ea53ece490c4b11216a306213b16fd5ea))
* **build:** restore x86_64 native library support ([#385](https://github.com/vase4kin/TeamCityApp/issues/385)) ([9fa3d95](https://github.com/vase4kin/TeamCityApp/commit/9fa3d95610ffec154baeb87c235bdcce6a83b15f))
* **builds:** fetch running and queued snapshots in bulk ([#441](https://github.com/vase4kin/TeamCityApp/issues/441)) ([892efac](https://github.com/vase4kin/TeamCityApp/commit/892efacb68557cd0eba1dd0e1a09cc78f6c634a7))
* **home:** keep count retries clear of system bars ([#430](https://github.com/vase4kin/TeamCityApp/issues/430)) ([b81766c](https://github.com/vase4kin/TeamCityApp/commit/b81766c43732dca2b886d6502d6dfc0e7a2fc772))
* **release:** start the 2.0 beta release sequence ([#443](https://github.com/vase4kin/TeamCityApp/issues/443)) ([7bb4a0f](https://github.com/vase4kin/TeamCityApp/commit/7bb4a0f372e3f5c0a840d38be7a300369ee19ee7))
* **ui:** align empty screens with the shared message card ([#432](https://github.com/vase4kin/TeamCityApp/issues/432)) ([7dc7d32](https://github.com/vase4kin/TeamCityApp/commit/7dc7d32db7188dd9b0c50d58dcfa96467e4cab0a))
* **ui:** align loading indicators with M3 Expressive ([#436](https://github.com/vase4kin/TeamCityApp/issues/436)) ([af0a03b](https://github.com/vase4kin/TeamCityApp/commit/af0a03b2eabd648c1b2cb4e58acaefcd080674fa))
* **ui:** improve floating toolbar visibility ([#437](https://github.com/vase4kin/TeamCityApp/issues/437)) ([29dfc1d](https://github.com/vase4kin/TeamCityApp/commit/29dfc1dfe6fb8f197b1ac626cc313aca5afd0861))
* **ui:** keep app bars pinned and lift on scroll ([#435](https://github.com/vase4kin/TeamCityApp/issues/435)) ([0d50717](https://github.com/vase4kin/TeamCityApp/commit/0d50717185f97756f4a256694cd536f97c61cb0e))
* **ui:** unify Compose FABs and correct form backgrounds and ripples ([826dffc](https://github.com/vase4kin/TeamCityApp/commit/826dffc401da640bdff9482af5654aa885bdb0d9))
