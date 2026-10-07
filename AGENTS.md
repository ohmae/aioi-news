# AGENTS.md

相生市の公式フィードを取得・キャッシュし、新着情報とリンク集を表示するAndroidアプリ。
このファイルには、コードからは分からない作業方針と、変更時に守るべき制約を記載する。

## 作業方針

- 説明・確認・完了報告、作成するドキュメント、著作権表示を除くコードコメントは、指定がなければ日本語にする。
- Markdownは、表やURLなど改行に適さない箇所を除き、半角120文字程度を目安に改行する。
- 書式は `.editorconfig` に従い、既存のドキュメントコメントを不必要に削除・改変しない。
- ビルドや実行時にエラーが起きた場合は、ログとスタックトレースを確認して原因を特定する。
- 外部API・フィードの仕様、新機能のデザイン・遷移に不明点があれば、
  該当する作業を始める前にユーザーへ確認する。既存コードだけで判断できる事項は確認不要。
- 変更に応じた既存テストの選択と必要な回帰テストの追加は、エージェントの判断で進める。
  実機・エミュレーターなどの検証環境の要件が不明な場合は、ユーザーへ確認する。

## 実装上の制約

- データ取得の状態はViewModel / StateFlowで公開し、PagerやDrawerなどのUI状態はCompose側で管理する。
- バックスタックの変更は `Navigator` に集約する。画面追加時はNavKey・遷移グラフ・Entry定義を更新する。
  `NavigationDisplay` の状態保存、ViewModelのスコープ、予測型戻る、遷移中の操作の保留を維持する。
- 依存ライブラリは `gradle/libs.versions.toml` で管理する。
- Roomの構造を変更した場合は、マイグレーションと `app/schemas/` の更新を確認する。
- Androidフレームワークを使うローカルテストでは `androidx.test` のAPIを優先する。
  `org.robolectric` の直接利用は、対応するAPIがない場合に限る
  （例: マニフェスト未登録のActivityを制御する `Robolectric.buildActivity`）。
- 新規・変更するテストメソッド名は `<テスト対象> <日本語での説明>` とする。

## 検証

リポジトリルートでGradle Wrapperを使用する。

| 変更内容 | 必要な確認 |
| --- | --- |
| コード変更全般 | `./gradlew ktlint` と `./gradlew :app:assembleDebug`、変更に関係するテスト |
| ローカルテスト | `./gradlew :app:testDebugUnitTest`（必要に応じて `--tests` で対象を限定） |
| Android Lintに関係する変更 | `./gradlew :app:lintDebug` |
| 依存関係の変更 | `./gradlew :app:dependencyGuard` |
| ドキュメントのみ | 記載内容と実ファイルの整合性、差分の確認。ビルドは不要 |

- `ktlint` は `isIgnoreExitValue = true` のため、Gradleの成功表示だけでなく違反の出力を確認する。
  フォーマットには `./gradlew ktlintFormat` を使い、対象外の変更が入っていないか差分を確認する。
- 画面遷移を変更した場合は `NavigatorTest` と `NavigationDisplayTest` を実行する。
- Dependency Guardの差分が意図した変更であることを確認してから、
  `./dependency-guard-baseline.sh` でベースラインを更新する。
- 完了報告には実施した検証と結果を記載し、未実施・失敗した検証があれば理由を明記する。

## 必要なときに参照する情報

SDK・JVM・ライブラリのバージョンや詳細なファイル一覧はここに複製せず、実ファイルを確認する。

- ビルド設定: `app/build.gradle.kts`、`baseline-profile/build.gradle.kts`、
  `gradle/libs.versions.toml`、`gradle/wrapper/gradle-wrapper.properties`。
- 実装: `app/src/main/kotlin/net/mm2d/news/` 配下の `core/`（モデル・インターフェース）、
  `data/`（データ取得・保存）、`aioi/ui/`（画面）。テストは `app/src/test/kotlin/`。
- 依存更新候補の生成: `./version-catalog-update.sh`。
  既存の `gradle/libs.versions.updates.toml` は削除・再生成されるため、必要な内容を事前に確認する。
- Baseline Profileの生成: `./gradlew :app:generateBaselineProfile`。
  使用する端末と接続端末の利用可否は `baseline-profile/build.gradle.kts` を確認する。
