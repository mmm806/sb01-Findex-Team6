package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.dto.AutoSyncConfigDto;
import com.sprint.findex_team6.dto.request.AutoSyncConfigCursorPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface AutoIntegrationQuerydslRepository {

  Slice<AutoSyncConfigDto> cursorBasePagination(AutoSyncConfigCursorPageRequest request, Pageable slice);
}
