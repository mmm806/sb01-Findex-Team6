package com.sprint.findex_team6.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record IndexDataSyncRequest(
    List<Integer> indexInfoIds,

    @NotNull
    LocalDate baseDateFrom,

    @NotNull
    LocalDate baseDateTo
) {

}
