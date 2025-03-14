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
  @Mapping(source = "date", target = "baseDate")
  @Mapping(source = "index", target = "indexInfoId" , qualifiedByName = "indexInfoIdByIndex")
  @Mapping(source = "tradingQuantity", target = "tradingQuantity", qualifiedByName = "longToInt")
  @Mapping(source = "tradingPrice" , target = "tradingPrice", qualifiedByName = "decimalToInt")
  @Mapping(source = "marketTotalCount", target = "marketTotalAmount", qualifiedByName = "decimalToInt")
  public IndexDataDto toDto(IndexVal indexVal);
}


//
//private LocalDate date;         // 기준 일자
//
//@Enumerated(EnumType.STRING)
//private SourceType sourceType;      // 소스 타입
//
//private BigDecimal marketPrice; // 시가
//private BigDecimal closePrice; // 종가
//private BigDecimal highPrice; // 고가
//private BigDecimal lowPrice;  // 저가
//private BigDecimal versus;       // 대비
//private BigDecimal fluctuationRate; // 등락률
//private Long tradingQuantity;             // 거래량
//private BigDecimal tradingPrice; // 거래대금
//private BigDecimal marketTotalCount;    // 상장 시가 총액
//
//Long id,
//Long indexInfoId,
//LocalDateTime baseDate,
//SourceType sourceType,
//BigDecimal marketPrice,
//BigDecimal closingPrice,
//BigDecimal highPrice,
//BigDecimal lowPrice,
//BigDecimal versus,
//BigDecimal fluctuationRate,
//Integer tradingQuantity,
//Integer tradingPrice,
//Integer marketTotalAmount