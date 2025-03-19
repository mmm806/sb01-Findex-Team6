package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.dto.dashboard.IndexValDto;
import com.sprint.findex_team6.entity.IndexVal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface IndexValMapper {
  IndexValMapper INSTANCE = Mappers.getMapper(IndexValMapper.class);

  @Mapping(source = "index.id",target = "id")
  IndexValDto toDto(IndexVal indexVal);
}
