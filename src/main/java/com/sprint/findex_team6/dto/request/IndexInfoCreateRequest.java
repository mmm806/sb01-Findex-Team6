package com.sprint.findex_team6.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoCreateRequest(
    String indexClassification,
    String indexName,
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    boolean favorite
) {
}
