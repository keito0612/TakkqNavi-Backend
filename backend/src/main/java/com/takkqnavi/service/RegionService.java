package com.takkqnavi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.takkqnavi.entity.Region;
import com.takkqnavi.repository.RegionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;

    public List<Region> getRegions() {
        return regionRepository.findAllByOrderById();
    }
}
