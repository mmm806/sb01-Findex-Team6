package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.IndexDataLink;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataLinkRepository extends JpaRepository<IndexDataLink, Long>,
    IndexDataLinkRepositoryQuerydsl {

  @EntityGraph(attributePaths = {"index"})
  List<IndexDataLink> findByIndex_IdAndTargetDateAndJobTime(Long indexInfoId, LocalDate targetDate, LocalDateTime jobTime);

  @EntityGraph(attributePaths = {"index"})
  List<IndexDataLink> findByIndex_Id(Long indexId);
}
