package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexVal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexValRepository extends JpaRepository<IndexVal, Long>{

  List<IndexVal> findByIndexIdInAndBaseDateIn(List<Long> indexIds, List<LocalDate> startDate);

  boolean existsByIndexIdAndBaseDate(Long indexId, LocalDate localDate);

  Optional<IndexVal> findByIndexAndBaseDate(Index index, LocalDate baseDate);

  List<IndexVal> findByIndexInAndBaseDateIn(List<Index> indexList, List<LocalDate> startDate);

  List<IndexVal> findByIndexAndBaseDateBetweenOrderByBaseDateAsc(Index index, LocalDate startDate, LocalDate endDate);

}
