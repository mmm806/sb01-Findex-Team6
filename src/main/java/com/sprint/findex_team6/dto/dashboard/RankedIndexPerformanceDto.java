package com.sprint.findex_team6.dto.dashboard;

public record RankedIndexPerformanceDto (
    IndexPerformanceDto performance,//지수 성과 정보
    Integer rank //순위
){  }
