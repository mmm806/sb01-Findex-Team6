package com.sprint.findex_team6.dto.controller;


import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.request.IndexInfoCreateRequest;
import com.sprint.findex_team6.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.service.IndexService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-infos")
public class IndexInfoController {

  private final IndexService indexService;

  @PostMapping
  public ResponseEntity<IndexInfoDto> create(@RequestBody IndexInfoCreateRequest request){
    IndexInfoDto index = indexService.create(request);

    if(index == null){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(index);
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
}
