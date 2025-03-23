package com.sprint.findex_team6.dto.request;

public record IndexInfoQueryRequest(
        String indexClassification,
        String indexName,
        Boolean favorite,
        String cursor,
        Long idAfter
) {
}
