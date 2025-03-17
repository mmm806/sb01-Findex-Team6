package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.PeriodType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexValRepository extends JpaRepository<IndexVal, Long> {
  List<IndexVal> findByIndex_Id(Long indexId, PeriodType periodType);
  List<IndexVal> findByPeriodType(PeriodType periodType);

}
