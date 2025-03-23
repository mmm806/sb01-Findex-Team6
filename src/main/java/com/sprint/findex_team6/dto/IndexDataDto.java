package com.sprint.findex_team6.dto;

import com.sprint.findex_team6.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataDto(
    Long id, //지수 데이터 ID
    Long indexInfoId, // 지수 정보 ID
    LocalDate baseDate, // 기준 일자
    SourceType sourceType, // 출처
    BigDecimal marketPrice, // 시가
    BigDecimal closingPrice, // 종가
    BigDecimal highPrice, // 고가
    BigDecimal lowPrice, // 저가
    BigDecimal versus, // 전일 대비 등락
    BigDecimal fluctuationRate, // 전일 대비 등락률
    Long tradingQuantity, // 거래량
    Long tradingPrice, // 거래대금
    Long marketTotalAmount // 상장 시가 총액

) {

}
