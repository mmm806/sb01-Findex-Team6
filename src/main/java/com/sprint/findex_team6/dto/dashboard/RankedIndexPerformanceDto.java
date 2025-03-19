package com.sprint.findex_team6.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

public record RankedIndexPerformanceDto(
    @Schema(description = "지수 성과 정보")
    IndexPerformanceDto dto,

    @Schema(description = "순위", example = "1")
    int rank
) {

}
