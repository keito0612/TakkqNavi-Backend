# TakkQ Navi

卓球大会情報ナビゲーションサービス

## 概要

| 項目 | 内容 |
|------|------|
| アプリ名 | TakkQ Navi |
| コンセプト | 一般・パラ卓球の大会情報を一元管理 |
| ターゲット | 卓球プレイヤー（一般・障害者） |

## 解決したい課題

- 障害者向けの卓球大会情報を扱うWebサービスがない
- 大会情報が散在していて探しにくい
- 申込期限を見逃しがち

## 機能一覧

### MVP

| 機能 | 説明 |
|------|------|
| カテゴリ切替 | 一般 / パラ |
| 大会一覧 | リスト表示 |
| 大会詳細 | 概要 + PDFダウンロード |
| 地域フィルター | 地域で絞り込み |
| 種目タグフィルター | 性別・年齢・形式・クラスで絞り込み |
| 検索 | キーワード検索 |
| AI収集 | 月1回自動収集 |
| 期限切れ削除 | 日次で申込期限切れを削除 |

## 技術スタック

### フロントエンド

| 項目 | 技術 |
|------|------|
| フレームワーク | Next.js (App Router) |
| 言語 | TypeScript |
| スタイリング | Tailwind CSS |
| UIコンポーネント | shadcn/ui |
| データフェッチング | TanStack Query |
| 状態管理 | useContext |
| バリデーション | Zod |
| フォーム | React Hook Form |
| HTTPクライアント | fetch |
| 日付処理 | date-fns |
| テスト | Vitest + Testing Library |
| デプロイ | Vercel |

### バックエンド

| 項目 | 技術 |
|------|------|
| フレームワーク | Spring Boot 3 |
| 言語 | Java 21 |
| ビルド | Gradle |
| DB（本番） | PostgreSQL (Supabase) |
| DB（開発） | H2 Database (インメモリ) |
| ストレージ | Cloudflare R2 |
| AI | Claude API |
| デプロイ | Render |

### 環境別DB構成

| 環境 | DB | 説明 |
|------|-----|------|
| 開発 (dev) | H2 Database | インメモリ、設定不要、高速 |
| テスト (test) | H2 Database | テスト用、毎回リセット |
| 本番 (prod) | PostgreSQL (Supabase) | クラウド、永続化 |

#### 開発環境 (application-dev.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:takkq_dev
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
```

#### 本番環境 (application-prod.yml)

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
```

#### プロファイル切り替え

```bash
# 開発環境で起動
./gradlew bootRun --args='--spring.profiles.active=dev'

# 本番環境で起動
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### バッチ・インフラ

| 項目 | 技術 |
|------|------|
| スリープ防止 | GitHub Actions (14分ごとping) |
| バッチ処理 | Spring Scheduler |

## アーキテクチャ

### フロントエンド（レイヤードアーキテクチャ 4層）

```
┌─────────────────────────────────┐
│  Presentation層（UI）           │
│  - ページ、コンポーネント         │
├─────────────────────────────────┤
│  Application層（ユースケース）    │
│  - hooks、状態管理              │
├─────────────────────────────────┤
│  Domain層（ビジネスロジック）     │
│  - 型定義、エンティティ、Service  │
├─────────────────────────────────┤
│  Infrastructure層（外部連携）    │
│  - API呼び出し、Repository      │
└─────────────────────────────────┘
```

### フォルダ構成（フロントエンド）

```
src/
├── app/
│   ├── page.tsx
│   ├── tournaments/[id]/page.tsx
│   ├── layout.tsx
│   └── providers.tsx
│
├── presentation/
│   └── components/
│       ├── tournament/
│       │   ├── TournamentCard.tsx
│       │   ├── TournamentList.tsx
│       │   └── TournamentDetail.tsx
│       └── filter/
│           ├── CategoryToggle.tsx
│           ├── TagFilter.tsx
│           ├── RegionSelect.tsx
│           └── SearchInput.tsx
│
├── application/
│   ├── hooks/
│   │   ├── useTournaments.ts
│   │   └── useTournamentDetail.ts
│   └── contexts/
│       └── ServiceContext.tsx
│
├── domain/
│   ├── entities/
│   │   ├── Tournament.ts
│   │   └── Tag.ts
│   ├── services/
│   │   ├── TournamentService.ts
│   │   └── interfaces/
│   │       └── ITournamentService.ts
│   └── types/
│       └── filter.ts
│
├── infrastructure/
│   ├── api/
│   │   ├── ApiClient.ts
│   │   └── interfaces/
│   │       └── IApiClient.ts
│   └── repositories/
│       ├── TournamentRepository.ts
│       └── interfaces/
│           └── ITournamentRepository.ts
│
└── shared/
    ├── components/
    │   ├── Button.tsx
    │   ├── Loading.tsx
    │   ├── Modal.tsx
    │   ├── Tag.tsx
    │   └── Header.tsx
    ├── utils/
    │   ├── dateFormatter.ts
    │   └── stringUtils.ts
    ├── constants/
    │   └── regions.ts
    └── types/
        └── common.ts
```

### バックエンド（レイヤードアーキテクチャ）

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

## DB設計

### tournaments（大会）

| カラム | 型 | 説明 |
|--------|-----|------|
| id | UUID | PK |
| name | TEXT | 大会名 |
| category | TEXT | general / para |
| date | DATE | 開催日 |
| venue | TEXT | 会場 |
| address | TEXT | 住所 |
| region | bigInt | FK → regions |地域|
| entry_deadline | DATE | 申込期限 |
| summary | TEXT | 概要 |
| outline_pdf_url | TEXT | 要項PDF URL |
| entry_pdf_url | TEXT | 申込PDF URL |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

### tag_categories（タグカテゴリ）

| カラム | 型 | 説明 |
|--------|-----|------|
| id | UUID | PK |
| name | TEXT | gender / age / format / class |
| display_name | TEXT | 性別 / 年齢 / 形式 / クラス |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

### tags（タグ）

| カラム | 型 | 説明 |
|--------|-----|------|
| id | UUID | PK |
| category_id | UUID | FK → tag_categories |
| name | TEXT | 男子 / 一般 / シングルス など |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

### tournament_tags（中間テーブル）

| カラム | 型 | 説明 |
|--------|-----|------|
| id | UUID | PK |
| tournament_id | UUID | FK → tournaments |
| tag_id | UUID | FK → tags |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

### タグ一覧

| カテゴリ | タグ |
|----------|------|
| 性別 (gender) | 男子 / 女子 / 混合 |
| 年齢 (age) | 一般 / ジュニア / マスターズ |
| 形式 (format) | シングルス / ダブルス / 団体 |
| クラス (class) | 車いす / 立位 / 知的障害 |

## API設計

### 公開API

| メソッド | エンドポイント | 説明 |
|----------|----------------|------|
| GET | `/api/tournaments` | 大会一覧（フィルター・検索） |
| GET | `/api/tournaments/{id}` | 大会詳細 |
| GET | `/api/tags` | タグ一覧（カテゴリ別） |
| GET | `/api/regions` | 地域一覧 |
| GET | `/health` | ヘルスチェック |

### 管理API（AI収集用）

| メソッド | エンドポイント | 説明 |
|----------|----------------|------|
| POST | `/api/admin/tournaments` | 大会登録 |
| PUT | `/api/admin/tournaments/{id}` | 大会更新 |
| DELETE | `/api/admin/tournaments/{id}` | 大会削除 |

### クエリパラメータ（GET /api/tournaments）

```
?category=general|para
&region=tokyo
&gender=male,female
&age=general,junior
&format=singles,doubles
&class=wheelchair
&keyword=オープン
&page=1
&size=20
```

### レスポンス例（GET /api/tournaments）

```json
{
  "content": [
    {
      "id": "uuid",
      "name": "○○オープン大会",
      "category": "general",
      "date": "2026-11-15",
      "venue": "東京体育館",
      "region": "tokyo",
      "entryDeadline": "2026-11-01",
      "tags": [
        { "category": "gender", "name": "男子" },
        { "category": "format", "name": "シングルス" }
      ]
    }
  ],
  "page": 1,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

## バッチ処理

| バッチ | 頻度 | 内容 |
|--------|------|------|
| 期限切れ削除 | 日次 0時 | entry_deadline < 今日 を物理削除 |
| AI収集 | 毎月1日 0時 | 大会情報を収集・登録 |

### 実装

- Spring Scheduler (`@Scheduled`) で実装
- Render無料枠のスリープ対策として GitHub Actions で14分ごとにping

## 画面構成

### トップ / 大会一覧 (`/`)

```
┌─────────────────────────────────┐
│  TakkQ Navi                     │
├─────────────────────────────────┤
│  [一般] [パラ]                   │
├─────────────────────────────────┤
│  地域:  [全国 ▼]                 │
│  性別:  [男子] [女子] [混合]      │
│  年齢:  [一般] [ジュニア] [マスターズ] │
│  形式:  [シングルス] [ダブルス] [団体] │
├─────────────────────────────────┤
│  🔍 [キーワード検索...]           │
├─────────────────────────────────┤
│  ┌─────────────────────────┐    │
│  │ ○○オープン大会          │    │
│  │ 📅 2026/11/15           │    │
│  │ 📍 東京体育館            │    │
│  │ ⏰ 申込期限: 11/01       │    │
│  │ [男子] [シングルス]       │    │
│  └─────────────────────────┘    │
└─────────────────────────────────┘
```

### 大会詳細 (`/tournaments/[id]`)

```
┌─────────────────────────────────┐
│  ← 戻る                         │
├─────────────────────────────────┤
│  ○○オープン大会                 │
│  [男子] [一般] [シングルス]       │
├─────────────────────────────────┤
│  📅 開催日: 2026/11/15          │
│  📍 会場: 東京体育館             │
│  ⏰ 申込期限: 2026/11/01        │
├─────────────────────────────────┤
│  概要                           │
│  ─────────────────────────────  │
│  大会の概要テキストがここに       │
│  表示されます...                 │
├─────────────────────────────────┤
│  [📄 大会要項PDFをダウンロード]   │
│  [📝 申込書PDFをダウンロード]    │
└─────────────────────────────────┘
```

## 開発環境（Docker）

### 必要なもの

- Docker
- Docker Compose

### フロントエンド起動

```bash
cd takkq-navi-frontend

# 起動
docker-compose up

# バックグラウンド起動
docker-compose up -d

# 停止
docker-compose down

# ログ確認
docker-compose logs -f
```

http://localhost:3000 でアクセス可能

### バックエンド起動

```bash
cd takkq-navi-backend

# 起動
docker-compose up

# バックグラウンド起動
docker-compose up -d

# 停止
docker-compose down

# ログ確認
docker-compose logs -f
```

http://localhost:8080 でアクセス可能

### 両方同時に起動

```bash
# 別ターミナルで実行
cd takkq-navi-frontend && docker-compose up -d
cd takkq-navi-backend && docker-compose up -d
```

### H2 Console（開発用DB）

バックエンド起動後、以下でアクセス可能：

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:takkq_dev`
- Username: `sa`
- Password: （空欄）

## インフラ費用

| サービス | 費用 |
|----------|------|
| Vercel | 無料 |
| Render | 無料 |
| Supabase | 無料（500MB） |
| Cloudflare R2 | 無料（10GB） |
| GitHub Actions | 無料 |
| Claude API | 月数十円（月1回収集） |

**合計: ほぼ無料（月数十円程度）**
