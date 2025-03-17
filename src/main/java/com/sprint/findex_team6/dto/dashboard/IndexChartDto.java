package com.sprint.findex_team6.dto.dashboard;

import com.sprint.findex_team6.entity.PeriodType;
import java.util.List;

public record IndexChartDto(
  Long indexInfoId,
  String indexClassification,
  String indexName,
  PeriodType periodType,
  List<ChartDataPoint> dataPoints,
  List<ChartDataPoint> ma5DataPoints,
  List<ChartDataPoint> ma20DataPoints

) {

  public IndexChartDto() {
    this(null, null, null, null, List.of(), List.of(), List.of());
  }
}