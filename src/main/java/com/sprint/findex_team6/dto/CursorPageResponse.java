package com.sprint.findex_team6.dto;

import java.util.List;

public record CursorPageResponse<T> (
    List<T> content,
    Object nextCursor,
    Long nextIdAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {

}
