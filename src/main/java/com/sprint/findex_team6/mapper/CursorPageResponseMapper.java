package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.dto.CursorPageResponse;
import com.sprint.findex_team6.dto.IndexDataDto;
import java.util.List;

import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.response.CursorPageResponseIndexInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
public class CursorPageResponseMapper {

  public CursorPageResponse<IndexDataDto> fromPageIndexDataDto(Page<IndexDataDto> page) {
    List<IndexDataDto> content = page.getContent();
    Sort sort = page.getSort();
    Object nextCursor = null;
    Long nextIdAfter = null;

    if (!content.isEmpty()) {
      Order order = sort.iterator().next();
      if (order.getProperty().equals("baseDate")) {
        nextCursor = content.get(content.size() - 1).baseDate();
      } else if (order.getProperty().equals("closingPrice")) {
        nextCursor = content.get(content.size() - 1).closingPrice();
      }
      nextIdAfter = content.get(content.size() - 1).id();
    }

    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextIdAfter,
        page.getSize(),
        page.getTotalElements(),
        page.hasNext()
    );
  }

  public CursorPageResponse<IndexInfoDto> fromPageIndexInfoDto(Page<IndexInfoDto> page) {
    List<IndexInfoDto> content = page.getContent();
    Sort sort = page.getSort();
    Object nextCursor = null;
    Long nextIdAfter = null;

    if (!content.isEmpty()) {
      Order order = sort.iterator().next();
      if (order.getProperty().equals("indexClassification")) {
        nextCursor = content.get(content.size() - 1).indexClassification();
      } else if (order.getProperty().equals("indexName")) {
        nextCursor = content.get(content.size() - 1).indexName();
      }
      nextIdAfter = content.get(content.size() - 1).id();
    }

    return new CursorPageResponse<>(
            content,
            nextCursor,
            nextIdAfter,
            page.getSize(),
            page.getTotalElements(),
            page.hasNext()
    );
  }

}
