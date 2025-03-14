package com.sprint.findex_team6.mapper;


import com.sprint.findex_team6.dto.IndexDataDto;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.SourceType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {IndexValMapperHelper.class})
@Named("IndexValMapper")
public interface IndexValMapper {

  @Mapping(source = "closePrice" , target = "closingPrice")
  @Mapping(source = "date", target = "baseDate")
  @Mapping(source = "index", target = "indexInfoId" , qualifiedByName = "indexInfoIdByIndex")
  @Mapping(source = "tradingQuantity", target = "tradingQuantity", qualifiedByName = "longToInt")
  @Mapping(source = "tradingPrice" , target = "tradingPrice", qualifiedByName = "decimalToInt")
  @Mapping(source = "marketTotalCount", target = "marketTotalAmount", qualifiedByName = "decimalToInt")
  IndexDataDto toDto(IndexVal indexVal);
}
