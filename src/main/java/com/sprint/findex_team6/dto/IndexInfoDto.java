package com.sprint.findex_team6.dto;

import com.sprint.findex_team6.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoDto(
    Long id,
    String indexClassification,
    String indexName,
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    SourceType sourceType,
    boolean favorite
){

}
