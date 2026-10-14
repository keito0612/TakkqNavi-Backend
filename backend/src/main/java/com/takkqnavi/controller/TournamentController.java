package com.takkqnavi.controller;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.takkqnavi.dto.TournamentSearchParam;
import com.takkqnavi.entity.Tournament;
import com.takkqnavi.exception.ResourceNotFoundException;
import com.takkqnavi.service.TournamentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentService tournamentService;

    /** 大会一覧取得 */
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<Tournament>> list(
            @PathVariable Long regionId,
            @Valid @ModelAttribute TournamentSearchParam param) {
        Sort sort = "desc".equals(param.getOrder())
                ? Sort.by(param.getSortBy()).descending()
                : Sort.by(param.getSortBy()).ascending();

        List<Tournament> tournaments = tournamentService.search(regionId, param, sort);
        return ResponseEntity.ok(tournaments);
    }

    /** 大会詳細取得 */
    @GetMapping("/{id}")
    public ResponseEntity<Tournament> detail(@PathVariable UUID id) {
        Tournament tournament = tournamentService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + id));
        return ResponseEntity.ok(tournament);
    }
}
