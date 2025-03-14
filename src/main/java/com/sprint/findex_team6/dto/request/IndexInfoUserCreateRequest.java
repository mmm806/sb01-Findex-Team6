package com.sprint.findex_team6.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoUserCreateRequest(
    String indexClassification,
    String indexName,
    int employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    boolean favorite
) {
}
