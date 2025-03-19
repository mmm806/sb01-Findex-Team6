package com.sprint.findex_team6.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "차트 데이터 포인트 DTO")
public record ChartDataPoint(
    @Schema(description = "날짜", example = "2025-03-18")
    LocalDate baseDate,

    @Schema(description = "값", example = "2000.23")
    BigDecimal value
) {

}
