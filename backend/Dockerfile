# 開発用Dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Gradleラッパーのコピー
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# 依存関係のインストール（キャッシュ活用）
COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon || true

# ソースコードのコピー
COPY src src

# ポート公開
EXPOSE 8080

# 開発サーバー起動
CMD ["./gradlew", "bootRun", "--args=--spring.profiles.active=dev", "--no-daemon"]
