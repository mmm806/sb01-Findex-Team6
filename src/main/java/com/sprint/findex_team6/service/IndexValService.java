package com.sprint.findex_team6.service;

import static com.sprint.findex_team6.error.ErrorCode.INDEX_NOT_FOUND;

import com.sprint.findex_team6.dto.dashboard.ChartDataPoint;
import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex_team6.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.error.CustomException;
import com.sprint.findex_team6.repository.IndexRepository;
import com.sprint.findex_team6.repository.IndexValRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexValService {

  private final IndexValRepository indexValRepository;
  private final IndexRepository indexRepository;

  @Transactional(readOnly = true)
  public List<IndexPerformanceDto> getInterestIndexPerformance(String periodType) {
    List<Index> favoriteIndexes = indexRepository.findByFavorite(true);
    LocalDate startDate = calculateStartDate(periodType);
    LocalDate endDate = LocalDate.now();

    List<Long> indexIds = favoriteIndexes.stream().map(Index::getId).toList();

    List<IndexVal> indexValList = indexValRepository.findByIndexIdInAndBaseDateIn(
        indexIds, List.of(startDate, endDate));

    Map<Long, IndexVal> startDataMap = indexValList.stream()
        .filter(data -> data.getBaseDate().equals(startDate))
        .collect(Collectors.toMap(data -> data.getIndex().getId(), Function.identity()));

    Map<Long, IndexVal> endDataMap = indexValList.stream()
        .filter(data -> data.getBaseDate().equals(endDate))
        .collect(Collectors.toMap(data -> data.getIndex().getId(), Function.identity()));

    return favoriteIndexes.stream()
        .map(index -> createIndexPerformanceDto(index, startDataMap, endDataMap))
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
  }

  private LocalDate calculateStartDate(String periodType) {
    LocalDate endDate = LocalDate.now();
    return switch (periodType) {
      //case "DAILY" -> endDate;
      //case "WEEKLY" -> endDate.minusWeeks(12);
      case "MONTHLY" -> endDate.minusMonths(12);
      case "QUARTERLY" -> endDate.minusMonths(36);
      case "YEARLY" -> endDate.minusYears(5);
      default -> throw new IllegalStateException("Unexpected value: " + periodType);
    };
  }

  private Optional<IndexPerformanceDto> createIndexPerformanceDto(
      Index index, Map<Long, IndexVal> startDataMap, Map<Long, IndexVal> endDataMap) {
    IndexVal startData = startDataMap.get(index.getId());
    IndexVal endData = endDataMap.get(index.getId());

    if (startData != null && endData != null) {
      BigDecimal startPrice = startData.getClosingPrice();
      BigDecimal endPrice = endData.getClosingPrice();
      BigDecimal versus = endPrice.subtract(startPrice);
      BigDecimal fluctuationRate = versus.divide(startPrice, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));

      return Optional.of(new IndexPerformanceDto(
          index.getId().longValue(),
          index.getIndexClassification(),
          index.getIndexName(),
          versus,
          fluctuationRate,
          endPrice,
          startPrice
      ));
    }
    return Optional.empty();
  }

  public List<RankedIndexPerformanceDto> getIndexPerformanceRank(String periodType, Long indexInfoId, int limit) {
    LocalDate beforeDate = calculateStartDate(periodType);
    LocalDate today = LocalDate.now();

    Index targetIndexInfo = indexRepository.findById(indexInfoId)
        .orElseThrow(() -> new CustomException(INDEX_NOT_FOUND));

    List<Index> indexInfoList = indexRepository.findByIndexClassification(targetIndexInfo.getIndexClassification());

    List<IndexVal> indexDataList = indexValRepository.findByIndexInAndBaseDateIn(indexInfoList, List.of(beforeDate, today));

    Map<Long, IndexVal> beforeDataMap = indexDataList.stream()
        .filter(data -> data.getBaseDate().equals(beforeDate))
        .collect(Collectors.toMap(data -> data.getIndex().getId(), Function.identity()));

    Map<Long, IndexVal> currentDataMap = indexDataList.stream()
        .filter(data -> data.getBaseDate().equals(today))
        .collect(Collectors.toMap(data -> data.getIndex().getId(), Function.identity()));

    List<IndexPerformanceDto> performanceList = indexInfoList.stream()
        .map(indexInfo -> createIndexPerformanceDto(indexInfo, beforeDataMap, currentDataMap))
        .flatMap(Optional::stream)
        .sorted(Comparator.comparing(IndexPerformanceDto::fluctuationRate).reversed())
        .limit(limit)
        .toList();

    return IntStream.range(0, performanceList.size())
        .mapToObj(i -> new RankedIndexPerformanceDto(performanceList.get(i), i + 1))
        .limit(limit)
        .collect(Collectors.toList());
  }

  public IndexChartDto getIndexChart(String periodType, Long indexId) {
    LocalDate startDate = calculateStartDate(periodType);
    LocalDate endDate = LocalDate.now();

    Index indexInfo = indexRepository.findById(indexId)
        .orElseThrow(() -> new CustomException(INDEX_NOT_FOUND));

    List<IndexVal> indexDataList = indexValRepository
        .findByIndexAndBaseDateBetweenOrderByBaseDateAsc(indexInfo, startDate, endDate);

    List<ChartDataPoint> dataPoints = indexDataList.stream()
        .map(data -> new ChartDataPoint(data.getBaseDate(), data.getClosingPrice()))
        .toList();

    List<ChartDataPoint> ma5DataPoints = calculateMovingAverage(dataPoints, 5);
    List<ChartDataPoint> ma20DataPoints = calculateMovingAverage(dataPoints, 20);

    return new IndexChartDto(
        indexId,
        indexInfo.getIndexClassification(),
        indexInfo.getIndexName(),
        periodType,
        dataPoints,
        ma5DataPoints,
        ma20DataPoints
    );
  }

  private List<ChartDataPoint> calculateMovingAverage(List<ChartDataPoint> dataPoints, int period) {
    List<ChartDataPoint> maDataPoints = new ArrayList<>();
    for (int i = period - 1; i < dataPoints.size(); i++) {
      BigDecimal sum = BigDecimal.ZERO;
      for (int j = 0; j < period; j++) {
        sum = sum.add(dataPoints.get(i - j).value());
      }
      BigDecimal average = sum.divide(BigDecimal.valueOf(period), 4, BigDecimal.ROUND_HALF_UP);
      maDataPoints.add(new ChartDataPoint(dataPoints.get(i).baseDate(), average));
    }
    return maDataPoints;
  }
}
