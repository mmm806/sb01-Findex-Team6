package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex_team6.entity.IndexVal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface IndexValMapper {
  IndexValMapper INSTANCE = Mappers.getMapper(IndexValMapper.class);

  @Mapping(source = "index.id", target = "indexInfoId")
  @Mapping(source = "index.indexName", target = "indexName")
  IndexChartDto toDto(IndexVal indexVal); // 차트 조회


}
