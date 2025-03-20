package com.sprint.findex_team6.dto;

public record DashboardDto(
    Long id,
    Long indexId,
    Boolean favorite,
    Long performanceDataId //성과 분석 데이터 ID
) {

}