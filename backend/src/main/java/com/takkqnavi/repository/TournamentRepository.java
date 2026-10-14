package com.takkqnavi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.takkqnavi.entity.Tournament;

import java.util.UUID;

@Repository
public interface TournamentRepository extends
        JpaRepository<Tournament, UUID>,
        JpaSpecificationExecutor<Tournament> {
}
