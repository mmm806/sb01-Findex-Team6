package com.sprint.findex_team6.dto;


import com.sprint.findex_team6.entity.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SyncJobDto(
    Long id,
    ContentType jobType,
    Long indexInfoId,
    LocalDate targetDate,
    String worker,
    LocalDateTime jobTime,
    String result
) {

}
