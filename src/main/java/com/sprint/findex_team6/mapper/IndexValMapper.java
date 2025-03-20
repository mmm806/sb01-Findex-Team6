package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.dto.dashboard.IndexValDto;
import com.sprint.findex_team6.entity.IndexVal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {IndexValMapperHelper.class})
@Named("IndexValMapper")
public interface IndexValMapper {

  @Mapping(source = "index.id",target = "id")
  IndexValDto toDto(IndexVal indexVal);
  
  @Mapping(source = "index", target = "indexInfoId" , qualifiedByName = "indexInfoIdByIndex")
  @Mapping(source = "tradingPrice" , target = "tradingPrice", qualifiedByName = "decimalToLong")
  @Mapping(source = "marketTotalCount", target = "marketTotalAmount", qualifiedByName = "decimalToLong")
  IndexDataDto toDto(IndexVal indexVal);

}
