package com.takkqnavi.batch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.takkqnavi.entity.Region;
import com.takkqnavi.entity.Tournament;
import com.takkqnavi.repository.TournamentRepository;
import com.takkqnavi.service.ClaudeApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TournamentItemProcessor implements ItemProcessor<Region, List<Tournament>> {

    private final ClaudeApiService claudeApiService;
    private final TournamentRepository tournamentRepository;
    private final Environment environment;

    @Override
    public List<Tournament> process(Region region) throws Exception {
        log.info("Processing region: {}", region.getName());

        List<Tournament> tournaments;

        if (isDevProfile()) {
            // 開発環境: ダミーデータ
            tournaments = createDummyTournaments(region);
        } else {
            // 本番環境: Claude APIから取得
            tournaments = claudeApiService.fetchTournamentsForRegion(region, 6);
        }

        // 重複チェック（新規のみ返す）
        List<Tournament> newTournaments = new ArrayList<>();
        for (Tournament tournament : tournaments) {
            boolean exists = tournamentRepository.existsByNameAndStartDate(
                    tournament.getName(),
                    tournament.getStartDate()
            );
            if (!exists) {
                newTournaments.add(tournament);
            }
        }

        log.info("Region {}: found {} tournaments, {} are new",
                region.getName(), tournaments.size(), newTournaments.size());

        return newTournaments.isEmpty() ? null : newTournaments;
    }

    private boolean isDevProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equals(profile)) {
                return true;
            }
        }
        return false;
    }

    private List<Tournament> createDummyTournaments(Region region) {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                Tournament.builder()
                        .name(region.getName() + "バッチテスト大会 " + System.currentTimeMillis())
                        .category("general")
                        .startDate(LocalDate.now().plusDays(60))
                        .venue(region.getName() + "テスト会場")
                        .address(region.getName() + "テスト区1-1-1")
                        .region(region)
                        .entryDeadline(LocalDate.now().plusDays(30))
                        .summary("バッチ処理テスト用の大会です。")
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );
    }
}
