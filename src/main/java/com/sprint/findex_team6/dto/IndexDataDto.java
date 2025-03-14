package com.sprint.findex_team6.dto;

import com.sprint.findex_team6.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IndexDataDto(
    Long id,
    Long indexInfoId,
    LocalDateTime baseDate,
    SourceType sourceType,
    BigDecimal marketPrice,
    BigDecimal closingPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    Integer tradingQuantity,
    Integer tradingPrice,
    Integer marketTotalAmount

) {

}
