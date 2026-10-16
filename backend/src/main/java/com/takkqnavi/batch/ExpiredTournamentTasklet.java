package com.takkqnavi.batch;

import java.time.LocalDate;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.takkqnavi.entity.Tournament;
import com.takkqnavi.repository.TournamentRepository;
import com.takkqnavi.service.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredTournamentTasklet implements Tasklet {

    private final TournamentRepository tournamentRepository;
    private final StorageService storageService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Starting expired tournament cleanup...");

        LocalDate today = LocalDate.now();
        List<Tournament> expired = tournamentRepository.findByEntryDeadlineBefore(today);

        if (expired.isEmpty()) {
            log.info("No expired tournaments found.");
            return RepeatStatus.FINISHED;
        }

        // PDFファイルを削除
        int deletedFiles = 0;
        for (Tournament tournament : expired) {
            deletedFiles += deletePdfIfExists(tournament.getOutlinePdfUrl());
            deletedFiles += deletePdfIfExists(tournament.getEntryPdfUrl());
        }
        log.info("Deleted {} PDF files.", deletedFiles);

        // DBレコードを削除
        tournamentRepository.deleteAll(expired);
        log.info("Deleted {} expired tournaments.", expired.size());

        // 削除件数をジョブ実行コンテキストに記録
        contribution.incrementWriteCount(expired.size());

        return RepeatStatus.FINISHED;
    }

    /**
     * PDFファイルが存在する場合は削除
     * @return 削除した場合は1、それ以外は0
     */
    private int deletePdfIfExists(String url) {
        String path = storageService.extractPath(url);
        if (path != null) {
            try {
                storageService.delete(path);
                return 1;
            } catch (Exception e) {
                log.warn("Failed to delete PDF: {}", path, e);
            }
        }
        return 0;
    }
}
