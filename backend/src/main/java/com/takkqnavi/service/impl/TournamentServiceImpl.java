package com.takkqnavi.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.takkqnavi.dto.TournamentSearchParam;
import com.takkqnavi.entity.Tournament;
import com.takkqnavi.repository.TournamentRepository;
import com.takkqnavi.service.TournamentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {
    private final TournamentRepository tournamentRepository;

    @Override
    public Optional<Tournament> getById(UUID id) {
        return tournamentRepository.findById(id);
    }

    @Override
    public List<Tournament> search(Long regionId, TournamentSearchParam param, Sort sort) {
        // 地域（必須）
        Specification<Tournament> spec = (root, query, cb) ->
                cb.equal(root.get("region").get("id"), regionId);

        // キーワード検索（大会名・地域名・会場名・住所）
        if (param.getKeyword() != null && !param.getKeyword().isEmpty()) {
            String keyword = "%" + param.getKeyword() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("name"), keyword),
                            cb.like(root.get("region").get("name"), keyword),
                            cb.like(root.get("venue"), keyword),
                            cb.like(root.get("address"), keyword)
                    ));
        }

        // 開催日範囲（任意）
        if (param.getFrom() != null && param.getTo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("startDate"), param.getFrom(), param.getTo()));
        }

        return tournamentRepository.findAll(spec, sort);
    }

    @Override
    public Tournament create(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    @Override
    public void createAll(List<Tournament> tournaments) {
        tournamentRepository.saveAll(tournaments);
    }

    @Override
    public void delete(UUID id) {
        tournamentRepository.deleteById(id);
    }
}
