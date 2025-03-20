package com.sprint.findex_team6.dto.request;

public record AutoSyncConfigCursorPageRequest(
    Long indexInfoId,
    Boolean enabled,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {

}
