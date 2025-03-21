package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.dto.IndexDataDto;
import com.sprint.findex_team6.entity.IndexVal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {IndexValMapperHelper.class})
public interface IndexValMapper {
  @Mapping(source = "index", target = "indexInfoId" , qualifiedByName = "indexInfoIdByIndex")
  @Mapping(source = "tradingPrice" , target = "tradingPrice", qualifiedByName = "decimalToLong")
  @Mapping(source = "marketTotalAmount", target = "marketTotalAmount", qualifiedByName = "decimalToLong")
  IndexDataDto toDto(IndexVal indexVal);
}
