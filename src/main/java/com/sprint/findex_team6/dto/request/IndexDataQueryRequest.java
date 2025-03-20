package com.sprint.findex_team6.dto.request;

import java.time.LocalDate;

public record IndexDataQueryRequest(
    Long indexInfoId,
    LocalDate startDate,
    LocalDate endDate,
    String cursor,
    Long idAfter
) {

}
