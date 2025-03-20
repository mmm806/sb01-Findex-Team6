package com.sprint.findex_team6.controller;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.CursorPageRequest;
import com.sprint.findex_team6.dto.request.IndexDataSyncRequest;
import com.sprint.findex_team6.dto.response.CursorPageResponseSyncJobDto;
import com.sprint.findex_team6.service.SyncDataJobsService;
import com.sprint.findex_team6.service.SyncInfoJobsService;
import com.sprint.findex_team6.service.SyncJobsSearchService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : wongil
 * @package : com.sprint.findex_team6.controller
 * @name : SyncJobsController.java
 * @date : 2025-03-13 오후 9:10
 * @Description: 연동 작업 관련 Controller [지수 정보 연동, 지수 데이터 연동, 연동 작업 목록 조회]
 **/
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobsController {

  private final SyncInfoJobsService syncInfoJobsService;
  private final SyncDataJobsService syncDataJobsService;
  private final SyncJobsSearchService syncJobsSearchService;

  /**
   * @methodName : syncInfo
   * @date : 2025-03-13 오후 8:59
   * @author : wongil
   * @Description: 지수 정보 연동
   **/
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping("/index-infos")
  public List<SyncJobDto> syncInformation(HttpServletRequest request) {

    return syncInfoJobsService.syncInfo(request);
  }

  /**
   * @methodName : syncData
   * @date : 2025-03-13 오후 9:00
   * @author : wongil
   * @Description: 지수 '데이터' 연동
   **/
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping("/index-data")
  public List<SyncJobDto> syncData(@RequestBody @Validated IndexDataSyncRequest request,
      HttpServletRequest httpRequest) {

    return syncDataJobsService.syncData(request, httpRequest);
  }


  /**
  * @methodName : findSyncJob
  * @date : 2025-03-17 오후 2:38
  * @author : wongil
  * @Description: 여러 검색 조건에 따라 연동 정보 또는 데이터 조회
  **/
  @GetMapping
  public CursorPageResponseSyncJobDto findSyncJob(@ModelAttribute("request") CursorPageRequest request, Pageable pageable) {

    return syncJobsSearchService.search(request, pageable);
  }

}
