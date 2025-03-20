package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.entity.IndexDataLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IndexDateLinkMapper {

  @Mapping(source = "jobType", target = "jobType")
  @Mapping(source = "index.id", target = "indexInfoId")
  SyncJobDto toDto(IndexDataLink indexDataLink);
}
