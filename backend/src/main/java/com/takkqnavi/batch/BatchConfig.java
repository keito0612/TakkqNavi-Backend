package com.takkqnavi.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.takkqnavi.entity.Region;
import com.takkqnavi.entity.Tournament;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final TournamentItemReader tournamentItemReader;
    private final TournamentItemProcessor tournamentItemProcessor;
    private final TournamentItemWriter tournamentItemWriter;
    private final ExpiredTournamentTasklet expiredTournamentTasklet;
    private final TournamentJobListener tournamentJobListener;

    /**
     * 大会取得ジョブ
     */
    @Bean
    public Job tournamentFetchJob(JobRepository jobRepository, Step fetchTournamentsStep) {
        return new JobBuilder("tournamentFetchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(tournamentJobListener)
                .start(fetchTournamentsStep)
                .build();
    }

    /**
     * 大会取得ステップ（チャンク処理）
     */
    @Bean
    public Step fetchTournamentsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("fetchTournamentsStep", jobRepository)
                .<Region, java.util.List<Tournament>>chunk(10, transactionManager)
                .reader(tournamentItemReader)
                .processor(tournamentItemProcessor)
                .writer(tournamentItemWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }

    /**
     * 期限切れ削除ジョブ
     */
    @Bean
    public Job expiredTournamentCleanupJob(JobRepository jobRepository, Step cleanupExpiredStep) {
        return new JobBuilder("expiredTournamentCleanupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cleanupExpiredStep)
                .build();
    }

    /**
     * 期限切れ削除ステップ（タスクレット）
     */
    @Bean
    public Step cleanupExpiredStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("cleanupExpiredStep", jobRepository)
                .tasklet(expiredTournamentTasklet, transactionManager)
                .build();
    }
}
