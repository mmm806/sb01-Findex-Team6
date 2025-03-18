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
          @RequestParam(defaultValue = "indexClassification") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @RequestParam(required = false) Long idAfter,
          Pageable pageable
  ) {
    CursorPageResponseIndexInfoDto<IndexInfoDto> response = indexService.getIndexInfos(
            indexClassification, indexName, favorite, sortField, sortDirection, idAfter, pageable
    );
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> getIndexSummaries() {
    List<IndexInfoSummaryDto> summaries = indexService.getIndexSummaries();
    return ResponseEntity.status(HttpStatus.OK).body(summaries);
  }


}
