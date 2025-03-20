package com.sprint.findex_team6.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexValCsvDto(
    LocalDate baseDate,
    BigDecimal marketPrice,
    BigDecimal closingPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    Long tradingQuantity,
    BigDecimal tradingPrice,
    BigDecimal marketTotalAmount

) {

}
