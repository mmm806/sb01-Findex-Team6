package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.PeriodType;
import com.sprint.findex_team6.repository.IndexValRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexValService {
  private final IndexValRepository indexValRepository;

  public IndexChartDto getChartData(Long id, PeriodType periodType){
    List<IndexVal> indexValList = indexValRepository.findByIndex_Id(id,periodType);
    return new IndexChartDto();
  }
  public List<IndexPerformanceDto> getFavoriteIndexPerformance(PeriodType periodType) {
    List<IndexVal> favoriteIndexes = indexValRepository.findByPeriodType(periodType);
    return favoriteIndexes.stream().map(indexVal -> new IndexPerformanceDto(
            indexVal.getIndex().getId(),
            indexVal.getIndex().getIndexClassification(),
            indexVal.getIndex().getIndexName(),
            indexVal.getVersus(),
            indexVal.getFluctuationRate(),
            indexVal.getClosePrice(),
            indexVal.getMarketPrice()
        ))
        .collect(Collectors.toList());
  }

}
