package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.SyncCursorPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface IndexDataLinkQuerydslRepository {

  Slice<SyncJobDto> cursorBasePagination(SyncCursorPageRequest request, Pageable pageable);

  Long cursorBasePaginationTotalCount(SyncCursorPageRequest request);

}
