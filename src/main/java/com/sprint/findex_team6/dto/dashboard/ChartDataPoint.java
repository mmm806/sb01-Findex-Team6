package com.sprint.findex_team6.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChartDataPoint (
    LocalDate baseDate,
    BigDecimal closingPrice
){ //차트 데이터 포인트

}
