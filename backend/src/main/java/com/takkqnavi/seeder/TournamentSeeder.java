package com.takkqnavi.seeder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.takkqnavi.entity.Region;
import com.takkqnavi.entity.Tournament;
import com.takkqnavi.repository.RegionRepository;
import com.takkqnavi.repository.TournamentRepository;
import com.takkqnavi.service.ClaudeApiService;
import com.takkqnavi.service.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class TournamentSeeder implements CommandLineRunner {

    private final TournamentRepository tournamentRepository;
    private final RegionRepository regionRepository;
    private final ClaudeApiService claudeApiService;
    private final StorageService storageService;
    private final Environment environment;

    @Override
    public void run(String... args) {
        if (tournamentRepository.count() > 0) {
            log.info("Tournaments already seeded. Skipping...");
            return;
        }

        String activeProfile = getActiveProfile();

        if ("prod".equals(activeProfile)) {
            seedFromCloudCode();
        } else {
            seedDevData();
        }
    }

    private String getActiveProfile() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length > 0 ? profiles[0] : "dev";
    }

    /** 本番: Claude APIから半年分の大会情報を取得 */
    private void seedFromCloudCode() {
        log.info("Seeding tournaments from Claude API (6 months)...");

        List<Tournament> tournaments = claudeApiService.fetchTournaments(6);

        if (tournaments.isEmpty()) {
            log.warn("No tournaments fetched from Claude API.");
            return;
        }

        tournamentRepository.saveAll(tournaments);
        log.info("Seeded {} tournaments from Claude API.", tournaments.size());
    }

    /** 開発: テスト用サンプルデータ */
    private void seedDevData() {
        log.info("Seeding dev tournaments...");

        Region tokyo = regionRepository.findByName("東京都").orElse(null);
        Region osaka = regionRepository.findByName("大阪府").orElse(null);
        Region kanagawa = regionRepository.findByName("神奈川県").orElse(null);

        if (tokyo == null || osaka == null || kanagawa == null) {
            log.warn("Required regions not found. Skipping tournament seeding...");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // ダミーPDFをアップロード
        PdfUrls tokyoOpenPdf = uploadDummyPdfs("tokyo-open", "東京オープン");
        PdfUrls osakaPdf = uploadDummyPdfs("osaka-championship", "大阪選手権");
        PdfUrls kanagawaPdf = uploadDummyPdfs("kanagawa-para", "神奈川パラ");
        PdfUrls tokyoJuniorPdf = uploadDummyPdfs("tokyo-junior", "東京ジュニア");

        List<Tournament> tournaments = List.of(
                Tournament.builder()
                        .name("第50回東京オープン卓球大会")
                        .category("general")
                        .startDate(LocalDate.now().plusDays(30))
                        .venue("東京体育館")
                        .address("東京都渋谷区千駄ヶ谷1-17-1")
                        .region(tokyo)
                        .entryDeadline(LocalDate.now().plusDays(14))
                        .summary("東京都卓球連盟主催のオープン大会です。")
                        .outlinePdfUrl(tokyoOpenPdf.outline())
                        .entryPdfUrl(tokyoOpenPdf.entry())
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                Tournament.builder()
                        .name("大阪卓球選手権大会")
                        .category("general")
                        .startDate(LocalDate.now().plusDays(45))
                        .venue("大阪市中央体育館")
                        .address("大阪府大阪市港区田中3-1-40")
                        .region(osaka)
                        .entryDeadline(LocalDate.now().plusDays(30))
                        .summary("大阪府卓球協会主催の選手権大会です。")
                        .outlinePdfUrl(osakaPdf.outline())
                        .entryPdfUrl(osakaPdf.entry())
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                Tournament.builder()
                        .name("神奈川県パラ卓球大会")
                        .category("para")
                        .startDate(LocalDate.now().plusDays(60))
                        .venue("横浜国際プール")
                        .address("神奈川県横浜市都筑区北山田7-3-1")
                        .region(kanagawa)
                        .entryDeadline(LocalDate.now().plusDays(45))
                        .summary("パラ卓球の普及を目的とした大会です。")
                        .outlinePdfUrl(kanagawaPdf.outline())
                        .entryPdfUrl(kanagawaPdf.entry())
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                Tournament.builder()
                        .name("東京ジュニア卓球大会")
                        .category("general")
                        .startDate(LocalDate.now().plusDays(20))
                        .venue("駒沢オリンピック公園総合運動場")
                        .address("東京都世田谷区駒沢公園1-1")
                        .region(tokyo)
                        .entryDeadline(LocalDate.now().plusDays(7))
                        .summary("中学生・高校生を対象としたジュニア大会です。")
                        .outlinePdfUrl(tokyoJuniorPdf.outline())
                        .entryPdfUrl(tokyoJuniorPdf.entry())
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        tournamentRepository.saveAll(tournaments);

        log.info("Seeded {} dev tournaments.", tournaments.size());
    }

    /** PDF URL ペア */
    private record PdfUrls(String outline, String entry) {}

    /**
     * 大会要項と申込用紙のダミーPDFをアップロード
     */
    private PdfUrls uploadDummyPdfs(String baseName, String title) {
        String outline = uploadDummyPdf("tournaments/" + baseName + "-outline.pdf", title + "大会要項");
        String entry = uploadDummyPdf("tournaments/" + baseName + "-entry.pdf", title + "申込用紙");
        return new PdfUrls(outline, entry);
    }

    /**
     * ダミーPDFをアップロードしてURLを返す
     */
    private String uploadDummyPdf(String path, String title) {
        String dummyContent = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\n" +
                "trailer\n<< /Root 1 0 R >>\n%%EOF\n" +
                "% Dummy PDF: " + title;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                dummyContent.getBytes(StandardCharsets.UTF_8)
        );

        String url = storageService.upload(path, inputStream, "application/pdf");
        log.info("Uploaded dummy PDF: {} -> {}", path, url);
        return url;
    }
}
