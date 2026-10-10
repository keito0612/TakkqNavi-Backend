# TakkQ Navi - Backend

## プロジェクト概要

卓球大会情報ナビゲーションサービスのバックエンド

## 技術スタック

- Java 21
- Spring Boot 3
- Gradle
- H2 Database（開発）
- PostgreSQL（本番 - Supabase）
- Cloudflare R2（ストレージ）
- Claude API（AI収集）

## アーキテクチャ

レイヤードアーキテクチャ

```
src/main/java/com/takkqnavi/
├── TakkqNaviApplication.java
├── controller/           # コントローラー層
├── service/              # サービス層（ビジネスロジック）
├── repository/           # リポジトリ層（データアクセス）
├── entity/               # エンティティ
├── dto/                  # DTO
├── batch/                # バッチ処理
└── config/               # 設定
```

## コーディング規約

- Lombokを使用してボイラープレートを削減
- Service、Repositoryはインターフェースと実装を分離
- DTOとEntityは明確に分離
- 例外は適切にハンドリング

## コマンド

```bash
# 開発環境で起動
./gradlew bootRun --args='--spring.profiles.active=dev'

# Docker起動
docker-compose up

# テスト
./gradlew test

# ビルド
./gradlew build
```

## 環境別設定

| 環境 | プロファイル | DB |
|------|-------------|-----|
| 開発 | dev | H2（インメモリ） |
| テスト | test | H2 |
| 本番 | prod | PostgreSQL |

## API一覧

### 公開API

- GET `/api/tournaments` - 大会一覧
- GET `/api/tournaments/{id}` - 大会詳細
- GET `/api/tags` - タグ一覧
- GET `/api/regions` - 地域一覧
- GET `/health` - ヘルスチェック

### 管理API

- POST `/api/admin/tournaments` - 大会登録
- PUT `/api/admin/tournaments/{id}` - 大会更新
- DELETE `/api/admin/tournaments/{id}` - 大会削除

## バッチ処理

- 日次 0時: 申込期限切れ大会の削除
- 毎月1日 0時: AI収集

## H2 Console（開発用）

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:takkq_dev`
- Username: `sa`
- Password: （空欄）

## 関連リポジトリ

- フロントエンド: takkq-navi-frontend
