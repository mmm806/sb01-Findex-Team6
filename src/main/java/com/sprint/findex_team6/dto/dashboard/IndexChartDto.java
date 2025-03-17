package com.sprint.findex_team6.dto.dashboard;

import com.sprint.findex_team6.entity.PeriodType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndexChartDto {
  private Long indexInfoId;
  private String indexClassification;
  private String indexName;
  private PeriodType periodType;
  private List<ChartDataPoint> dataPoints;
  private List<ChartDataPoint> ma5DataPoints;
  private List<ChartDataPoint> ma20DataPoints;
}