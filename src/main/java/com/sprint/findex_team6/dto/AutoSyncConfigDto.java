package com.sprint.findex_team6.dto;

public record AutoSyncConfigDto(
    Long id,
    Long indexInfoId,
    String indexClassification,
    String indexName,
    boolean enable
) {
}
