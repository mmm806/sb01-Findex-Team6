package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.IndexDataLink;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataLinkRepository extends JpaRepository<IndexDataLink, Long>,
    IndexDataLinkRepositoryQuerydsl {

  IndexDataLink findByIndex_IdAndTargetDateAndJobTime(Long indexInfoId, LocalDate targetDate, LocalDateTime jobTime);

}
