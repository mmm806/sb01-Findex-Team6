package com.sprint.findex_team6.controller;

import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex_team6.entity.PeriodType;
import com.sprint.findex_team6.service.IndexValService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
  public IndexChartDto getChart(@PathVariable Long id, @RequestParam(defaultValue = "DAILY")
      PeriodType periodType) {
    return indexValService.getChartData(id, periodType);
  }
  @GetMapping("/performance/favorite")
  public List<IndexPerformanceDto> getFavoritePerformance(@RequestParam(defaultValue = "DAILY") PeriodType periodType) {
    return indexValService.getFavoriteIndexPerformance(periodType);
  }


}
