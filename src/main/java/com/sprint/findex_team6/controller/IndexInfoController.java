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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
          @RequestParam(required = false) String cursor,
          @RequestParam(required = false) Long idAfter,
          @RequestParam(defaultValue = "indexClassification") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @RequestParam(defaultValue = "10") int size  // 페이지 크기 (기본값 10)
  ) {
    Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
    Pageable pageable = PageRequest.of(0, size, sort);

    CursorPageResponseIndexInfoDto<IndexInfoDto> response =
            indexService.getIndexInfos(indexClassification, indexName, favorite, cursor, idAfter, sortField, sortDirection, size, pageable);

    return ResponseEntity.ok(response);
  }


  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> getIndexSummaries() {
    List<IndexInfoSummaryDto> summaries = indexService.getIndexSummaries();
    return ResponseEntity.status(HttpStatus.OK).body(summaries);
  }


}
