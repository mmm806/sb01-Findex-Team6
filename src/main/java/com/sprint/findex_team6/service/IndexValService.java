package com.sprint.findex_team6.service;

import static com.sprint.findex_team6.error.ErrorCode.INDEX_NOT_FOUND;

import com.opencsv.CSVWriter;
import com.sprint.findex_team6.dto.dashboard.ChartDataPoint;
import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex_team6.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.error.CustomException;
import com.sprint.findex_team6.repository.IndexRepository;
import com.sprint.findex_team6.repository.IndexValRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexValService {

  private final IndexValRepository indexValRepository;
  private final IndexRepository indexRepository;

  //관심지수 성과 조회
  @Transactional(readOnly = true)
  public List<IndexPerformanceDto> getInterestIndexPerformance(String periodType) {
    List<Index> favoriteIndexes = indexRepository.findByFavorite(true);

    //조회할 시작, 종료 날짜 설정
    LocalDate startDate = calculateStartDate(periodType);
    LocalDate endDate = LocalDate.now();

    //즐겨찾기 한 지수 ID목록 추출
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

  // 기간 타입에 따른 시작 날짜 계산
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

  //지수의 성과 데이터 생성
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

  //특정 지수 성과 순위 조회
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

  //특정 지수 차트 데이터 조회
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

  /**
   * CSV 파일 생성 및 응답
   */
  public void exportIndexDataToCsv(Long indexInfoId, String startDateStr, String endDateStr,
      String sortField, String sortDirection, HttpServletResponse response) {

    // 날짜 파싱 (기본값: startDate = 1년 전, endDate = 오늘)
    LocalDate startDate = parseDateOrDefault(startDateStr, LocalDate.now().minusYears(1));
    LocalDate endDate = parseDateOrDefault(endDateStr, LocalDate.now());

    // 정렬 필드와 방향 설정
    Sort sort = getSort(sortField, sortDirection);

    // 데이터 조회
    List<IndexVal> indexData = indexValRepository.findByIndexIdAndBaseDateBetween(indexInfoId, startDate, endDate, sort);

    // CSV 파일 응답 설정
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=\"index_data.csv\"");

    try (PrintWriter writer = response.getWriter();
        CSVWriter csvWriter = new CSVWriter(writer)) {

      // CSV 헤더 작성
      String[] header = {"Index ID", "Base Date", "Closing Price"};
      csvWriter.writeNext(header);

      // 데이터 추가
      for (IndexVal data : indexData) {
        String[] row = {
            data.getIndex().getId().toString(),
            data.getBaseDate().toString(),
            data.getClosingPrice().toString()
        };
        csvWriter.writeNext(row);
      }

    } catch (Exception e) {
      throw new RuntimeException("CSV 파일을 생성하는 중 오류가 발생했습니다.", e);
    }
  }

  /**
   * 날짜 파싱 메서드 (유효하지 않으면 기본값 사용)
   */
  private LocalDate parseDateOrDefault(String dateStr, LocalDate defaultDate) {
    try {
      return dateStr != null ? LocalDate.parse(dateStr) : defaultDate;
    } catch (DateTimeParseException e) {
      return defaultDate;
    }
  }

  /**
   * 정렬 필드 및 방향 설정
   */
  private Sort getSort(String sortField, String sortDirection) {
    if (sortField == null || sortField.isBlank()) {
      sortField = "baseDate";
    }
    Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
    return Sort.by(direction, sortField);
  }
}
