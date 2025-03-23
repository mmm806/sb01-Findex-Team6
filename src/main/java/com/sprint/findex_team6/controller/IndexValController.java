package com.sprint.findex_team6.controller;


import com.sprint.findex_team6.dto.dashboard.IndexChartDto;
import com.sprint.findex_team6.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex_team6.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex_team6.service.IndexValService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.sprint.findex_team6.dto.CursorPageResponse;
import com.sprint.findex_team6.dto.IndexDataDto;
import com.sprint.findex_team6.dto.request.IndexDataCreateRequest;
import com.sprint.findex_team6.dto.request.IndexDataQueryRequest;
import com.sprint.findex_team6.dto.request.IndexDataSortField;
import com.sprint.findex_team6.dto.request.IndexDataUpdateRequest;
import com.sprint.findex_team6.dto.request.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-data")
public class IndexValController {

  private final IndexValService indexValService;
  
  @GetMapping("/{id}/chart")
  public ResponseEntity<IndexChartDto> getIndexChart(@PathVariable Long id, @RequestParam String periodType) {
    IndexChartDto indexChartDto = indexValService.getIndexChart(periodType, id);
    return ResponseEntity.status(HttpStatus.OK).body(indexChartDto);
  }

  @GetMapping("/performance/favorite")
  public ResponseEntity<List<IndexPerformanceDto>> getIndexFavoritePerformanceRank(
      @RequestParam String periodType
  ){
    List<IndexPerformanceDto> dto = indexValService.getInterestIndexPerformance(periodType);
    return ResponseEntity.status(HttpStatus.OK).body(dto);
  }

  @GetMapping("/performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getIndexPerformanceRank(
      @RequestParam(defaultValue = "DAILY") String periodType,
      @RequestParam(required = false) Long indexInfoId, //indexInfoId 선택 조건 추가
      @RequestParam(defaultValue = "10") int limit) {
    List<RankedIndexPerformanceDto> dto = indexValService
        .getIndexPerformanceRank(periodType, indexInfoId, limit);
    return ResponseEntity.status(HttpStatus.OK).body(dto);
  }

  @GetMapping("/export/csv") //Csv 파일 변환
  public void exportCsv(
       @RequestParam(required = false) Long indexInfoId,
       @RequestParam(required = false) String startDate,
       @RequestParam(required = false) String endDate,
       @RequestParam(required = false) String sortField,
       @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      HttpServletResponse response) {

    indexValService.exportIndexDataToCsv(indexInfoId, startDate, endDate, sortField, sortDirection, response);
  }

  @PostMapping
  public ResponseEntity<IndexDataDto> postIndexVal(@RequestBody IndexDataCreateRequest request) {
    IndexDataDto indexDataDto = indexValService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(indexDataDto);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<IndexDataDto> patchIndexVal(@PathVariable("id") Long id, @RequestBody IndexDataUpdateRequest request) {
    IndexDataDto update = indexValService.update(id, request);
    return ResponseEntity.status(HttpStatus.OK).body(update);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteIndexVal(@PathVariable Long id) {
    indexValService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexDataDto>> getIndexVal(
      @ModelAttribute IndexDataQueryRequest request,
      @RequestParam(required = false, defaultValue = "baseDate") IndexDataSortField sortField,
      @RequestParam(required = false, defaultValue = "desc") SortDirection sortDirection,
      @RequestParam(required = false, defaultValue = "10") int size
  ) {
    //요청 파라미터로 PageRequest 객체 생성
    Sort.Direction direction = sortDirection == SortDirection.asc ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(
        new Sort.Order(direction, sortField.toString()),
        new Sort.Order(direction, "id")
    );
    PageRequest pageRequest = PageRequest.of(0, size, sort);

    //service 레이어 호출
    CursorPageResponse<IndexDataDto> indexData = indexValService.findIndexData(request, pageRequest);
    return ResponseEntity.ok(indexData);
  }

}
