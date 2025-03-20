package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.dto.AutoSyncConfigDto;
import com.sprint.findex_team6.dto.request.AutoSyncConfigCursorPageRequest;
import java.util.List;

public interface AutoIntegrationQuerydslRepository {

  List<AutoSyncConfigDto> cursorBasePagination(AutoSyncConfigCursorPageRequest request);
}
