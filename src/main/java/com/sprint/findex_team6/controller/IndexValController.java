package com.sprint.findex_team6.controller;

import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex_team6.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex_team6.service.IndexValService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<IndexChartDto> getIndexChart(@PathVariable Long id, @RequestParam String periodType) {
    IndexChartDto indexChartDto = indexValService.getIndexChart(periodType, id);
    return ResponseEntity.status(HttpStatus.OK).body(indexChartDto);
  }

  @GetMapping("/performance/favorite")
  public ResponseEntity<List<IndexPerformanceDto>> getIndexFavoritePerformanceRank(
      @RequestParam String periodType
  ){
    List<IndexPerformanceDto> dto = indexValService.getInterestIndexPerformance(periodType);
    return ResponseEntity.status(HttpStatus.OK).body(dto);
  }

  @GetMapping("/performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getIndexPerformanceRank(
      @RequestParam String periodType,
      @RequestParam Long indexInfoId,
      @RequestParam int limit) {
    List<RankedIndexPerformanceDto> dto = indexValService.getIndexPerformanceRank(periodType, indexInfoId, limit);
    return ResponseEntity.status(HttpStatus.OK).body(dto);
  }
 /*
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
