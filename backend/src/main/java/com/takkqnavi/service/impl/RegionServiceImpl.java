package com.takkqnavi.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.takkqnavi.entity.Region;
import com.takkqnavi.repository.RegionRepository;
import com.takkqnavi.service.RegionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {
    private final RegionRepository regionRepository;

    @Override
    public List<Region> getRegions() {
        return regionRepository.findAllByOrderById();
    }
}
