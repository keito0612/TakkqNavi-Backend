package com.takkqnavi.batch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TournamentJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=== Job Started: {} ===", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("=== Job Completed Successfully: {} ===", jobExecution.getJobInstance().getJobName());
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("=== Job Failed: {} ===", jobExecution.getJobInstance().getJobName());
            jobExecution.getAllFailureExceptions().forEach(e ->
                    log.error("Failure: {}", e.getMessage())
            );
        }

        if (jobExecution.getEndTime() != null && jobExecution.getStartTime() != null) {
            long duration = java.time.Duration.between(
                    jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();
            log.info("Job Duration: {} ms", duration);
        }
    }
}
