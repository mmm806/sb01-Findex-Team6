package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.dto.dashboard.ChartDataPoint;
import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.PeriodType;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IndexChartMapper {

  @Mapping(source = "index.id", target = "indexInfoId")
  @Mapping(source = "index.indexClassification", target = "indexClassification")
  @Mapping(source = "index.indexName", target = "indexName")
  IndexChartDto toDto(Index index, PeriodType periodType,
      List<ChartDataPoint> dataPoints,
      List<ChartDataPoint> ma5DataPoints,
      List<ChartDataPoint> ma20DataPoints);
}

