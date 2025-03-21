package com.sprint.findex_team6.dto.response;

import java.util.List;

public record CursorPageResponseSyncDto<T>(
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {

}
