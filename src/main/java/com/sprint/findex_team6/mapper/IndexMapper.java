package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.IndexInfoSummaryDto;
import com.sprint.findex_team6.entity.Index;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IndexMapper {
  @Mapping(source = "baseDate", target = "basePointInTime")
  IndexInfoDto toDto(Index index);

  IndexInfoSummaryDto toSummaryDto(Index index);
}
