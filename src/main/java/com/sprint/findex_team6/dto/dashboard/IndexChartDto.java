package com.sprint.findex_team6.dto.dashboard;

import com.sprint.findex_team6.entity.PeriodType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.List;
public record IndexChartDto(
    Long indexInfoId,
    String indexClassification,
    String indexName,
    @Enumerated(EnumType.STRING)
    PeriodType periodType,
    List<ChartDataPoint> dataPoints,
    List<ChartDataPoint> ma5DataPoints,
    List<ChartDataPoint> ma20DataPoints

) {

}