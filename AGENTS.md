# AGENTS.md - AI Agent Operational Guidelines & Technical Reference

このドキュメントは、AI Agent（Antigravity, Copilot, Cursor等）が本プロジェクト（aioi-news）でコード修正や機能追加を行う際に参照・遵守すべき開発ガイドラインおよびプロジェクト仕様です。

## 1. プロジェクト概要 (Overview)
- **アプリ名**: 相生市新着情報アプリ (AIOI-News)
- **アプリケーションID**: `net.mm2d.news.aioi`
- **目的**: 兵庫県相生市の公式Webサイトで配信されるRSS/Atomフィードを取得・キャッシュ・表示するAndroidアプリ。
- **リポジトリ構成**: 単一の `app` アプリケーションモジュールおよび `baseline-profile` モジュールから構成。

## 2. エージェント運用原則 (Agent Principles)
1. **指示の厳格な遵守**: ユーザーからの明示的な指示や設計条件（フィルタリングルール、レイアウト境界等）は正確に反映すること。
2. **ログとスタック・トレースの確認**: ランタイムエラーやビルドエラー発生時は推測に頼らず、必ずログを取得・確認して根本原因を特定すること。
3. **既存スタイルの維持**: `ktlint` のルールに厳格に従い、リポジトリ全体のコードスタイルの一貫性を維持すること。既存のドキュメントコメントを不必要に削除・改変しないこと。
4. **検証の実施**: コード変更後は必ず `./gradlew ktlint` や `./gradlew assembleDebug` でエラーがないか確認すること。

## 3. 技術スタック (Tech Stack)
- **言語**: Kotlin
- **JVM Target**: Java 11
- **Compile SDK / Min SDK / Target SDK**: 37 (release) / 28 / 36
- **UI**: Jetpack Compose + Material 3 + Jetpack Navigation 3 (`androidx.navigation3`)
- **DI (依存注入)**: Hilt (`com.google.dagger:hilt-android`) + KSP
- **Database / キャッシュ**: Room Database (`androidx.room`)
- **Networking**: Ktor Client (`io.ktor:ktor-client-okhttp`) / OkHttp3
- **コードスタイル / フォーマッタ**: ktlint
- **依存関係管理**: Gradle Version Catalog (`gradle/libs.versions.toml`) + Dependency Guard (`dependency-guard-baseline.sh`)

## 4. ディレクトリ & パッケージ構成 (Project Structure)

ソースコードルート: `app/src/main/kotlin/`

- **`net/mm2d/news/core/`**: ドメインモデルおよびリポジトリインターフェース
  - `Link.kt`, `RssFeed.kt`, `RssItem.kt`
  - `LinkRepository.kt`, `RssRepository.kt`
- **`net/mm2d/news/data/`**: データレイヤーの実装（DB, RSS Parser, Network, DI Modules）
  - `rss/database/`: Room DAO, Entities (`RssFeedEntity`, `RssItemEntity`), Database
  - `rss/parser/`: Atom / RSS 1.0 / RSS 2.0 の自作XmlPullParser実装
  - `rss/converter/`: Entity ⇄ Domain Model 変換
  - `http/`: Ktor / OkHttp クライアントの設定モジュール
- **`net/mm2d/news/aioi/ui/`**: 画面実装 (Jetpack Compose)
  - `MainActivity.kt`: エントリーポイント
  - `NavigationRoot.kt`, `Navigator.kt`: Navigation 3 を用いた画面遷移制御
  - `WhatsNewPage.kt`, `WhatsNewViewModel.kt`: 新着情報一覧画面
  - `LinkPage.kt`, `LinkViewModel.kt`: リンク集画面
  - `LicenseScreen.kt`, `DrawerContent.kt`
  - `theme/`: Compose Theme, Typography, Color パレット
- **`net/mm2d/news/aioi/util/`**: 拡張関数・ヘルパークラス
  - `CustomTabsHelper.kt`, `Launcher.kt`, `ContextExtensions.kt` 等

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
./gradlew assembleDebug
```

### 依存関係ガードのベースライン更新
```bash
./dependency-guard-baseline.sh
```

### ライブラリバージョンの更新チェック
```bash
./version-catalog-update.sh
```

## 6. コーディング規約・デザインパターン (Coding Standards)

1. **Jetpack Compose**:
   - Composable関数は `@Composable` を付与し、パスカルケース（PascalCase）で命名する。
   - 状態（State）はViewModelで管理し、UIコンポーネントは可能な限りステートレスを維持する。
2. **Jetpack Navigation 3**:
   - 本プロジェクトでは Navigation 3 (`androidx.navigation3`) を使用して画面遷移およびスタックを管理する。
3. **Dependency Injection**:
   - ViewModelへの依存注入には `@HiltViewModel` を使用し、Hiltのモジュール定義 (`@Module`, `@InstallIn(...)`) を通じて依存関係を提供する。
4. **Data Layer & Repository**:
   - `core/` のインターフェースに対し、`data/` 配下で `Impl` クラスとして具象実装を記述する。
   - 非同期処理には Kotlin Coroutines / Flow を使用する。

## 7. 情報の確認・フィードバック (Feedback)
AI Agentが機能追加や改修を行う際、以下の点において曖昧さや不明点がある場合は、作業を開始する前にユーザーへ確認を行ってください。
- 外部API / RSSフィードの仕様変更や追加リクエスト
- 新機能の画面デザイン・遷移仕様
- ユニットテスト / UIテストの追加方針や検証環境の要件
