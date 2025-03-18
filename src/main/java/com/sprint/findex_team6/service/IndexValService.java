package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.dashboard.ChartDataPoint;
import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.PeriodType;
import com.sprint.findex_team6.mapper.IndexChartMapper;
import com.sprint.findex_team6.repository.IndexValRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexValService {

  private final IndexValRepository indexValRepository;
  private final IndexChartMapper indexChartMapper;

  //차트조히
  public IndexChartDto getIndexChart(Long indexInfoId, PeriodType periodType) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = calculateStartDate(periodType, endDate);

    List<IndexVal> indexVals = indexValRepository.findByIndexIdandDateRange(indexInfoId, startDate, endDate);

    // 이동 평균선 계산
    List<ChartDataPoint> ma5 = calculateMovingAverage(indexVals, 5);
    List<ChartDataPoint> ma20 = calculateMovingAverage(indexVals, 20);

    // 데이터 변환 (MapStruct 사용)
    List<ChartDataPoint> dataPoints = indexVals.stream()
        .map(val -> new ChartDataPoint(val.getBaseDate(), val.getClosingPrice()))
        .toList();

    return indexChartMapper.toDto(indexVals.get(0).getIndex(), periodType, dataPoints, ma5, ma20);
  }

  private LocalDate calculateStartDate(PeriodType periodType, LocalDate endDate) {
    return switch (periodType) {
      case DAILY -> endDate.minusDays(30);
      case WEEKLY -> endDate.minusWeeks(12);
      case MONTHLY -> endDate.minusMonths(12);
      case QUARTERLY -> endDate.minusMonths(36);
      case YEARLY -> endDate.minusYears(5);
    };
  }

  private List<ChartDataPoint> calculateMovingAverage(List<IndexVal> data, int days) {
    List<ChartDataPoint> result = new ArrayList<>();
    for (int i = days - 1; i < data.size(); i++) {
      BigDecimal sum = BigDecimal.ZERO;
      for (int j = i - days + 1; j <= i; j++) {
        sum = sum.add(data.get(j).getClosingPrice());
      }
      BigDecimal average = sum.divide(BigDecimal.valueOf(days), RoundingMode.HALF_UP);
      result.add(new ChartDataPoint(data.get(i).getBaseDate(), average));
    }
    return result;
  }
}



