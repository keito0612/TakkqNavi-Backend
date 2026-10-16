package com.takkqnavi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.takkqnavi.entity.Tournament;

@Repository
public interface TournamentRepository extends
        JpaRepository<Tournament, UUID>,
        JpaSpecificationExecutor<Tournament> {

    /** 申込期限が指定日より前の大会を取得 */
    List<Tournament> findByEntryDeadlineBefore(LocalDate date);

    /** 名前と開催日で存在チェック */
    boolean existsByNameAndStartDate(String name, LocalDate startDate);
}
