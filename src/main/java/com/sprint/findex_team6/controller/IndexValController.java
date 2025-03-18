package com.sprint.findex_team6.controller;

import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex_team6.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex_team6.entity.PeriodType;
import com.sprint.findex_team6.service.IndexValService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-val")
public class IndexValController {
  private final IndexValService indexValService;

  @GetMapping("/{id}/chart")
  public ResponseEntity<IndexChartDto> getChart(@PathVariable Long id, @RequestParam(defaultValue = "DAILY") PeriodType periodType) {

    return ResponseEntity.ok(indexValService.getIndexChart(id, periodType));
  }

  /*@GetMapping("/performance/favorite") //관심지수 조회
  public List<IndexPerformanceDto> getFavoritePerformance(@RequestParam(defaultValue = "DAILY") PeriodType periodType) {
    return indexValService.getFavoriteIndexPerformance(periodType);
  }
  @GetMapping("/performance/rank") //랭킹조회
  public List<RankedIndexPerformanceDto> getRankedPerformance(
      @RequestParam Long inedxInfoId,
      @RequestParam(defaultValue = "DAILY") PeriodType periodType,
      @RequestParam(defaultValue = "10") int limit) {
    return indexValService.getRankedIndexPerformance(inedxInfoId, periodType, limit);
  }

  @GetMapping("/export/csv") //Csv 파일 변환
  public ResponseEntity<ByteArrayResource> exportCsv(
      @RequestParam Long indexInfoId,
      @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate,
      @RequestParam(defaultValue = "baseDate") String sortField,
      @RequestParam(defaultValue = "desc") String sortDirection) {

    ByteArrayResource csvFile = indexValService.exportCsv(indexInfoId, startDate, endDate, sortField, sortDirection);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=index_data.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(csvFile);
  }*/

}
