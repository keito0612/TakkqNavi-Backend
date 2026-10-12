package com.takkqnavi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.takkqnavi.entity.Region;
import com.takkqnavi.service.RegionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    /** 地域一覧取得 */
    @GetMapping
    public ResponseEntity<List<Region>> getRegions() {
        List<Region> regions = regionService.getRegions();
        return ResponseEntity.ok(regions);
    }
}
