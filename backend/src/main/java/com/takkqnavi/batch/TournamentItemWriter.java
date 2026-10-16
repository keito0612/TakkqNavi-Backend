package com.takkqnavi.batch;

import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.takkqnavi.entity.Tournament;
import com.takkqnavi.repository.TournamentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TournamentItemWriter implements ItemWriter<List<Tournament>> {

    private final TournamentRepository tournamentRepository;

    @Override
    public void write(Chunk<? extends List<Tournament>> chunk) throws Exception {
        int totalSaved = 0;

        for (List<Tournament> tournaments : chunk) {
            if (tournaments != null && !tournaments.isEmpty()) {
                tournamentRepository.saveAll(tournaments);
                totalSaved += tournaments.size();
            }
        }

        log.info("Saved {} tournaments in this chunk", totalSaved);
    }
}
