package com.sprint.findex_team6.dto.response;

import com.sprint.findex_team6.dto.SyncJobDto;
import java.util.List;

public record CursorPageResponseSyncJobDto(
    List<SyncJobDto> content,
    String nextCursor,
    Long nextIdAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {

}
