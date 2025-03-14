package com.sprint.findex_team6.controller;

import com.sprint.findex_team6.dto.IndexDataDto;
import com.sprint.findex_team6.dto.request.IndexDataCreateRequest;
import com.sprint.findex_team6.dto.request.IndexDataUpdateRequest;
import com.sprint.findex_team6.service.IndexValService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-data")
public class IndexValController {

  private final IndexValService indexValService;

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
}
