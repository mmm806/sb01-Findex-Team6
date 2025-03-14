package com.sprint.findex_team6.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;

public record IndexInfoCreateRequest(
    String indexClassification,
    String indexName,
    int employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    boolean favorite
) {
}
