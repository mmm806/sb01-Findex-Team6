package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.CursorPageRequest;
import com.sprint.findex_team6.dto.response.CursorPageResponseSyncJobDto;
import com.sprint.findex_team6.repository.IndexDataLinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncJobsSearchService {

  private final IndexDataLinkRepository indexDataLinkRepository;

  public CursorPageResponseSyncJobDto search(CursorPageRequest request, Pageable slice) {

    // 페이징해서 데이터 가져오기
    Slice<SyncJobDto> pagedSlice = indexDataLinkRepository.cursorBasePagination(request, slice);

    long totalElementCount = indexDataLinkRepository.count();
    int size = getSize(request);

    List<SyncJobDto> content = pagedSlice.getContent();
    boolean hasNext = pagedSlice.hasNext();
    String nextCursor = null;
    Long nextIdAfter = null;

    if (hasNext && !content.isEmpty()) {
      SyncJobDto lastContent = content.get(content.size() - 1);
      nextIdAfter = lastContent.getId();

      if (!(request.getSortField() == null) && request.getSortField().equals("targetDate")) {
        nextCursor = String.valueOf(lastContent.getTargetDate());
      }
      else {
        nextCursor = String.valueOf(lastContent.getJobTime());
      }
    }

    return new CursorPageResponseSyncJobDto(
        content,
        nextCursor,
        nextIdAfter,
        size,
        totalElementCount,
        hasNext
        );
  }

  private int getSize(CursorPageRequest request) {
    return request.getSize() == null ? 10 : request.getSize();
  }

}
