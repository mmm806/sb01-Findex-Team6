package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex_team6.dto.dashboard.IndexValCsvDto;
import com.sprint.findex_team6.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.PeriodType;
import com.sprint.findex_team6.repository.IndexValRepository;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
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
  public List<RankedIndexPerformanceDto> getRankedIndexPerformance(Long indexInfoId, PeriodType periodType, int limit) {
    List<IndexPerformanceDto> performances = getFavoriteIndexPerformance(periodType);
    return performances.stream()
        .sorted((p1,p2) -> p2.versus().compareTo(p1.versus()))
        .limit(limit)
        .map(performance -> new RankedIndexPerformanceDto(performance, performances.indexOf(performance) + 1))
        .collect(Collectors.toList());
  }

  public ByteArrayResource exportCsv(Long indexId, LocalDate startDate, LocalDate endDate, String sortField, String sortDirection ) {
    List<IndexVal> indexVals = indexValRepository.findIndexDataForCsv(indexId, startDate, endDate, sortField);
    List<IndexValCsvDto> csvDtos = indexVals.stream()
        .map(iv -> new IndexValCsvDto(
            iv.getDate(),
            iv.getMarketPrice(),
            iv.getClosePrice(),
            iv.getHighPrice(),
            iv.getLowPrice(),
            iv.getVersus(),
            iv.getFluctuationRate(),
            iv.getTradingQuantity(),
            iv.getTradingPrice(),
            iv.getMarketTotalCount()
        )).collect(Collectors.toList());

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(outputStream);

    //csv header
    writer.println("baseDate, marketPrice, closePrice, highPrice, lowPrice, versus, fluctuationRate, tradingQuantity, tradingPrice, marketTotalAmount");

    //csv add
    for (IndexValCsvDto data : csvDtos) {
      writer.printf("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
          data.baseDate(),
          data.marketPrice(),
          data.closePrice(),
          data.highPrice(),
          data.lowprice(),
          data.versus(),
          data.fluctuationRate(),
          data.tradingQuantity(),
          data.tradingPrice(),
          data.marketTotalAmount()
      );
    }

    writer.flush();
    writer.close();

    return new ByteArrayResource(outputStream.toByteArray());
  }


}
