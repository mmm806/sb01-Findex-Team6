package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.CursorPageRequest;
import com.sprint.findex_team6.repository.IndexDataLinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncJobsSearchService {

  private final IndexDataLinkRepository indexDataLinkRepository;

  public List<SyncJobDto> search(CursorPageRequest request) {

    return indexDataLinkRepository.search(request);
  }

}
