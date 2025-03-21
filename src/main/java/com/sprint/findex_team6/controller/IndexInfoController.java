package com.sprint.findex_team6.controller;


import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.IndexInfoSummaryDto;
import com.sprint.findex_team6.dto.request.IndexInfoCreateRequest;
import com.sprint.findex_team6.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex_team6.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.service.IndexService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<CursorPageResponseIndexInfoDto<IndexInfoDto>> getIndexInfos(
          @RequestParam(required = false) String indexClassification,
          @RequestParam(required = false) String indexName,
          @RequestParam(required = false) Boolean favorite,
          @RequestParam(required = false) String cursor,  // 커서
          @RequestParam(required = false) Long idAfter,  // idAfter와 cursor 둘 중 하나를 사용할 수 있음
          @RequestParam(defaultValue = "indexClassification") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @RequestParam(defaultValue = "10") int size,  // 페이지 크기 (기본값 10)
          Pageable pageable
  ) {
    // idAfter 또는 cursor가 있으면 커서 기반 페이지네이션을 처리하고, 없으면 페이지 번호 기반 처리
    CursorPageResponseIndexInfoDto<IndexInfoDto> response = indexService.getIndexInfos(
            indexClassification, indexName, favorite, cursor, idAfter, sortField, sortDirection, size, pageable
    );
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }


  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> getIndexSummaries() {
    List<IndexInfoSummaryDto> summaries = indexService.getIndexSummaries();
    return ResponseEntity.status(HttpStatus.OK).body(summaries);
  }


}
