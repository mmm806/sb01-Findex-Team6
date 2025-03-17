package com.sprint.findex_team6.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexValCsvDto (  //csv로 내보낼 데이터 필드들
                                LocalDate baseDate,
                                BigDecimal marketPrice,
                                BigDecimal closePrice,
                                BigDecimal highPrice,
                                BigDecimal lowprice,
                                BigDecimal versus,
                                BigDecimal fluctuationRate,
                                Long tradingQuantity,
                                BigDecimal tradingPrice,
                                BigDecimal marketTotalAmount

){

}
