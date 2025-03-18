package com.sprint.findex_team6.dto.response;

import java.util.List;

public record CursorPageResponseIndexInfoDto<T>(
        List<T> content,
        String nextCursor,
        Long nextIdAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
