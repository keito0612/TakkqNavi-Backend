package com.takkqnavi.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.takkqnavi.dto.TournamentSearchParam;
import com.takkqnavi.entity.Tournament;

public interface TournamentService {
    Optional<Tournament> getById(UUID id);
    List<Tournament> search(Long regionId, TournamentSearchParam param, Sort sort);
    Tournament create(Tournament tournament);
    void createAll(List<Tournament> tournaments);
    void delete(UUID id);
}
