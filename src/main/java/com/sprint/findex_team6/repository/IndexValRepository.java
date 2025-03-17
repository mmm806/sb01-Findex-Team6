package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.PeriodType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexValRepository extends JpaRepository<IndexVal, Long> {
  List<IndexVal> findByIndex_Id(Long indexId, PeriodType periodType);
  List<IndexVal> findByPeriodType(PeriodType periodType);

  @Query("SELECT iv From IndexVal iv WHERE iv.index.id = :indexId " +
      "AND iv.date BETWEEN :startDate AND :endDate " +
      "ORDER BY " +
      "CASE WHEN :sortField = 'marketPrice' THEN iv.marketPrice END ASC, " +
      "CASE WHEN :sortField = 'closePrice' THEN iv.closePrice END ASC")
    //indexId, startDate, endDate로 필터링 후 sortField 로 정렬
  List<IndexVal> findIndexDataForCsv(
      @Param("indexId")Long indexId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("sortField") String sortField
  );
}
