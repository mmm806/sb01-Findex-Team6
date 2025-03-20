package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.CursorPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface IndexDataLinkRepositoryQuerydsl {

  Slice<SyncJobDto> cursorBasePagination(CursorPageRequest request, Pageable pageable);

  Long cursorBasePaginationTotalCount(CursorPageRequest request);

}
