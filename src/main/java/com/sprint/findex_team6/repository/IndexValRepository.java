package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.PeriodType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexValRepository extends JpaRepository<IndexVal, Long>,
    QuerydslPredicateExecutor<IndexVal> {

  @Query("SELECT i FROM IndexVal i WHERE i.index.id = :indexId and i.baseDate BETWEEN :startDate and :endDate ORDER BY i.baseDate ASC")
  List<IndexVal> findByIndexIdandDateRange(
      @Param("indexId") Long id,
      @Param("startDate")LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
}
