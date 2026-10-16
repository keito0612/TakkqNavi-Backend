package com.takkqnavi.batch;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import com.takkqnavi.entity.Region;
import com.takkqnavi.repository.RegionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TournamentItemReader implements ItemReader<Region> {

    private final RegionRepository regionRepository;
    private Iterator<Region> regionIterator;

    @Override
    public Region read() {
        if (regionIterator == null) {
            List<Region> regions = regionRepository.findAll();
            regionIterator = regions.iterator();
            log.info("Loaded {} regions for processing", regions.size());
        }

        if (regionIterator.hasNext()) {
            Region region = regionIterator.next();
            log.debug("Reading region: {}", region.getName());
            return region;
        }

        // 次回実行のためにリセット
        regionIterator = null;
        return null;
    }
}
