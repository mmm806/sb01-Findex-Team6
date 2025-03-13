package com.sprint.findex_team6.dto;


import com.sprint.findex_team6.entity.ConnectType;
import java.time.LocalDateTime;

public record SyncJobDto(
    Long id,
    ConnectType jobType,
    Long indexInfoId,
    LocalDateTime targetDate,
    String worker,
    LocalDateTime jobTime,
    boolean result
) {

}
