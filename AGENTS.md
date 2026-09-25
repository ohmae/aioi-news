# AGENTS.md - AI Agent Operational Guidelines & Technical Reference

このドキュメントは、AI Agent（Antigravity, Copilot, Cursor等）が本プロジェクト（aioi-news）でコード修正や機能追加を行う際に参照・遵守すべき開発ガイドラインおよびプロジェクト仕様です。

## 1. プロジェクト概要 (Overview)
- **アプリ名**: 相生市新着情報アプリ (AIOI-News)
- **アプリケーションID**: `net.mm2d.news.aioi`
- **Debug版のアプリケーションID**: `net.mm2d.news.aioi.debug`
- **目的**: 兵庫県相生市の公式Webサイトで配信されるRSS/Atomフィードを取得・キャッシュ・表示するAndroidアプリ。
- **リポジトリ構成**: 単一の `app` アプリケーションモジュールおよび `baseline-profile` モジュールから構成。

## 2. エージェント運用原則 (Agent Principles)
1. **指示の厳格な遵守**: ユーザーからの明示的な指示や設計条件（フィルタリングルール、レイアウト境界等）は正確に反映すること。
2. **ログとスタック・トレースの確認**: ランタイムエラーやビルドエラー発生時は推測に頼らず、必ずログを取得・確認して根本原因を特定すること。
3. **既存スタイルの維持**: `ktlint` のルールに厳格に従い、リポジトリ全体のコードスタイルの一貫性を維持すること。既存のドキュメントコメントを不必要に削除・改変しないこと。
4. **検証の実施**: コード変更後は `./gradlew ktlint` と `./gradlew :app:assembleDebug` を実行し、変更に関係するテストも確認すること。ktlintタスクは
   `isIgnoreExitValue = true` のため、Gradleの成功表示だけでなく違反の出力も確認すること。ドキュメントのみの変更では記載内容と実ファイルの整合性および差分を確認すること。

## 3. 技術スタック (Tech Stack)
- **言語**: Kotlin
- **ビルド**: Gradle Wrapper + Android Gradle Plugin（AGP）の組み込みKotlin。バージョンは
  `gradle/wrapper/gradle-wrapper.properties` と `gradle/libs.versions.toml` を参照する。
- **JVM Target**: Java 11
- **Compile SDK / Min SDK / Target SDK**: 37 (release) / 28 / 37（両モジュール共通）
- **UI**: Jetpack Compose + Material 3 + Jetpack Navigation 3 (`androidx.navigation3`)
- **DI (依存注入)**: Hilt (`com.google.dagger:hilt-android`) + KSP
- **Database / キャッシュ**: Room Database (`androidx.room`)
- **Networking**: Ktor Client (`io.ktor:ktor-client-okhttp`) / OkHttp3
- **テスト**: JUnit 4 + Robolectric + Compose UI Test / MockK / Truth / Coroutines Test。カバレッジ計測用にKoverを導入。
- **性能計測**: Baseline Profile + Macrobenchmark / UI Automator
- **コードスタイル / フォーマッタ**: ktlint
- **依存関係管理**: Gradle Version Catalog (`gradle/libs.versions.toml`) + Dependency Guard (`dependency-guard-baseline.sh`)

## 4. ディレクトリ & パッケージ構成 (Project Structure)

ソースコードルート: `app/src/main/kotlin/`

- **`net/mm2d/news/core/`**: ドメインモデルおよびリポジトリインターフェース
  - `Link.kt`, `RssFeed.kt`, `RssItem.kt`
  - `LinkRepository.kt`, `RssRepository.kt`
- **`net/mm2d/news/data/`**: データレイヤーの実装（DB, RSS Parser, Network, DI Modules）
  - `rss/database/`: Room DAO, Entities (`RssFeedEntity`, `RssItemEntity`), Database
  - `rss/parser/`: SAXParserを使ったAtom / RSS 1.0 / RSS 2.0のパーサーと形式別Handler
  - `rss/converter/`: Entity ⇄ Domain Model 変換
  - `http/`: Ktor / OkHttp クライアントの設定モジュール
  - `link/`: `assets/links.json` をKotlin Serializationで読み込むリンク集リポジトリ
- **`net/mm2d/news/aioi/ui/`**: 画面実装 (Jetpack Compose)
  - `MainActivity.kt`: エントリーポイント
  - `NavigationRoot.kt`: Main / LicenseのNavKey、遷移グラフ、画面定義
  - `Navigator.kt`: バックスタック管理、遷移可否判定、Lifecycleに応じた操作の保留
  - `NavigationDisplay.kt`: Navigation 3の表示、Entryの状態保存・ViewModel管理、予測型戻るの処理
  - `MainScreen.kt`: ドロワーと新着情報・リンク集を切り替える2ページのHorizontalPager
  - `WhatsNewPage.kt`, `WhatsNewViewModel.kt`: 新着情報一覧画面
  - `LinkPage.kt`, `LinkViewModel.kt`: リンク集画面
  - `LicenseScreen.kt`, `NestedScrollingWebView.kt`: ローカルHTMLによるライセンス表示
  - `DrawerContent.kt`: ドロワーの内容
  - `theme/`: `Theme.kt`（テーマ）、`NavigationSpec.kt`（画面遷移アニメーション）
  - `modifier/FadingEdge.kt`: フェード表示用Modifier
- **`net/mm2d/news/aioi/util/`**: 拡張関数・ヘルパークラス
  - `CustomTabsHelper.kt`, `Launcher.kt`, `ContextExtensions.kt` 等

その他の主要ファイル・ソースセット:

- `app/src/main/kotlin/net/mm2d/news/aioi/App.kt`: Applicationクラス
- `app/src/debug/`: Debug用Manifestと `DebugApp.kt`
- `app/src/main/assets/`: リンク集の `links.json` とライセンスの `license.html`
- `app/schemas/`: Roomのエクスポート済みスキーマ（現在のDBバージョンは1）
- `app/src/test/kotlin/net/mm2d/news/aioi/ui/`: `NavigatorTest.kt` と `NavigationDisplayTest.kt`
  。Robolectricを使用するローカルテスト。
- `baseline-profile/src/main/kotlin/`: `BaselineProfileGenerator.kt` と `StartupBenchmarks.kt`

新着情報のフィードURLは `WhatsNewViewModel.kt` の `URL` で定義している。

## 5. 主要開発コマンド (Build & Verification Commands)

### コードスタイルの確認 (ktlint)

```bash
./gradlew ktlint
```

### コードスタイルの自動フォーマット (ktlintFormat)

```bash
./gradlew ktlintFormat
```

### デバッグビルドの実行

```bash
./gradlew :app:assembleDebug
```

### ローカルテストとAndroid Lint

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

### 依存関係ガードの確認

```bash
./gradlew :app:dependencyGuard
```

### 依存関係ガードのベースライン更新

```bash
./dependency-guard-baseline.sh
```

対象は `releaseRuntimeClasspath`。意図した依存変更を確認してから更新する。スクリプトはConfiguration Cacheを無効にして実行する。

### ライブラリバージョンの更新候補生成

```bash
./version-catalog-update.sh
```

安定版を対象に `versionCatalogUpdate --interactive --no-configuration-cache` を実行する。既存の
`gradle/libs.versions.updates.toml` は削除して再生成する。

### Baseline Profileの生成

```bash
./gradlew :app:generateBaselineProfile
```

`baseline-profile/build.gradle.kts` のManaged Virtual Device `pixel10Api37`（Pixel 10 / API 37 / Googleイメージ / 16
KBページ）を使用する。`useConnectedDevices = false` のため接続済み端末は使わない。実行には対応するエミュレーター環境が必要。

## 6. コーディング規約・デザインパターン (Coding Standards)

1. **Jetpack Compose**:
   - Composable関数は `@Composable` を付与し、パスカルケース（PascalCase）で命名する。
   - データ取得に関する状態はViewModel / StateFlowで公開し、UIコンポーネントは可能な限りステートレスを維持する。PagerやDrawerなどのUI状態はCompose側で管理する。
2. **Jetpack Navigation 3**:
   - 本プロジェクトでは Navigation 3 (`androidx.navigation3`) を使用して画面遷移およびスタックを管理する。
   - バックスタックの変更は `Navigator` に集約する。画面追加時はNavKey、遷移グラフ、Entry定義を更新する。
   - `NavigationDisplay` による状態保存・ViewModelのスコープ・予測型戻るを維持し、遷移処理の変更時は既存のNavigationテストで検証する。
3. **Dependency Injection**:
   - ViewModelへの依存注入には `@HiltViewModel` を使用し、Hiltのモジュール定義 (`@Module`, `@InstallIn(...)`) を通じて依存関係を提供する。
4. **Data Layer & Repository**:
   - `core/` のインターフェースに対し、`data/` 配下で `Impl` クラスとして具象実装を記述する。
   - 非同期処理には Kotlin Coroutines / Flow を使用する。
5. **書式と依存関係**:
    - `.editorconfig` に従う（KotlinはIntelliJ IDEAスタイル、4スペース、最大120文字）。
    - 依存ライブラリは `gradle/libs.versions.toml` で管理し、Roomの構造変更時は `app/schemas/` も確認する。

## 7. 情報の確認・フィードバック (Feedback)
AI Agentが機能追加や改修を行う際、以下の点において曖昧さや不明点がある場合は、作業を開始する前にユーザーへ確認を行ってください。
- 外部API / RSSフィードの仕様変更や追加リクエスト
- 新機能の画面デザイン・遷移仕様
- ユニットテスト / UIテストの追加方針や検証環境の要件
