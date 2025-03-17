package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.dto.DashboardDto;
import com.sprint.findex_team6.entity.Dashboard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

  @Mapping(source = "index.id", target = "indexId")
  @Mapping(source = "performanceData.id", target = "performanceDataId")
  DashboardDto toDto (Dashboard dashboard);

}