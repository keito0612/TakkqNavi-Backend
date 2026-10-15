package com.takkqnavi.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class TournamentJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job tournamentFetchJob;
    private final Job expiredTournamentCleanupJob;

    /**
     * 期限切れ削除ジョブを実行
     * - dev: 毎分
     * - prod: 毎日0時
     */
    @Scheduled(cron = "${batch.cron.delete-expired:0 0 0 * * *}")
    public void runCleanupJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            log.info("Launching expiredTournamentCleanupJob...");
            jobLauncher.run(expiredTournamentCleanupJob, params);
        } catch (Exception e) {
            log.error("Failed to run cleanup job: {}", e.getMessage(), e);
        }
    }

    /**
     * 大会取得ジョブを実行
     * - dev: 5分ごと
     * - prod: 毎月1日0時
     */
    @Scheduled(cron = "${batch.cron.update-tournaments:0 0 0 1 * *}")
    public void runFetchJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            log.info("Launching tournamentFetchJob...");
            jobLauncher.run(tournamentFetchJob, params);
        } catch (Exception e) {
            log.error("Failed to run fetch job: {}", e.getMessage(), e);
        }
    }
}
