package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.CursorPageRequest;
import java.util.List;

public interface IndexDataLinkRepositoryQuerydsl {

  List<SyncJobDto> search(CursorPageRequest request);
}
