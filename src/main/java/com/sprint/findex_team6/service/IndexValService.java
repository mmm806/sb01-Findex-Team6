package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.PeriodType;
import com.sprint.findex_team6.repository.IndexValRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IndexValService {
  private final IndexValRepository indexValRepository;
  public IndexValService(IndexValRepository indexValRepository) {
    this.indexValRepository = indexValRepository;
  }

  public IndexChartDto getChartData(Long id, PeriodType periodType){
    List<IndexVal> indexValList = indexValRepository.findByIndexId(id);
    return new IndexChartDto();
  }

}
