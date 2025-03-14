package com.sprint.findex_team6.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoUpdateRequest(
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    Boolean favorite){

}
