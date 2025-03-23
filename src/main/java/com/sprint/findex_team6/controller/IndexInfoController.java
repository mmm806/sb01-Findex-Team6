package com.sprint.findex_team6.controller;


import com.sprint.findex_team6.dto.CursorPageResponse;
import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.IndexInfoSummaryDto;
import com.sprint.findex_team6.dto.request.*;
import com.sprint.findex_team6.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.service.IndexService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-infos")
public class IndexInfoController {

  private final IndexService indexService;

  @PostMapping
  public ResponseEntity<?> create(@RequestBody IndexInfoCreateRequest request){
    return indexService.create(request);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<IndexInfoDto> update(@RequestBody IndexInfoUpdateRequest request, @PathVariable Long id){
    IndexInfoDto indexInfoDto = indexService.update(request,id);
    return ResponseEntity.status(HttpStatus.OK).body(indexInfoDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id){
    return indexService.delete(id);
  }

  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoDto> getIndexInfoById(@PathVariable Long id) {
    IndexInfoDto indexInfoDto = indexService.getIndexInfoById(id);
    return ResponseEntity.status(HttpStatus.OK).body(indexInfoDto);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexInfoDto>> getIndexInfos(
          @ModelAttribute IndexInfoQueryRequest request,
          @RequestParam(defaultValue = "indexName") IndexSortField sortField,
          @RequestParam(defaultValue = "desc") SortDirection sortDirection,
          @RequestParam(defaultValue = "10") int size
  ) {
    Sort.Direction direction = (sortDirection == SortDirection.asc)
            ? Sort.Direction.ASC : Sort.Direction.DESC;

    Sort sort = Sort.by(
            new Sort.Order(direction, sortField.name()),
            new Sort.Order(direction, "id")
    );

    PageRequest pageRequest = PageRequest.of(0, size, sort);

    CursorPageResponse<IndexInfoDto> result = indexService.getIndexInfos(
            request, pageRequest, sortField, sortDirection
    );

    return ResponseEntity.ok(result);
  }



  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> getIndexSummaries() {
    List<IndexInfoSummaryDto> summaries = indexService.getIndexSummaries();
    return ResponseEntity.status(HttpStatus.OK).body(summaries);
  }


}
