package com.sprint.findex_team6.controller;

import com.sprint.findex_team6.dto.CursorPageResponseSyncJobDto;
import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.IndexDataSyncRequest;
import com.sprint.findex_team6.entity.ContentType;
import com.sprint.findex_team6.service.SyncJobsService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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

  private final SyncJobsService syncJobsService;

  /**
   * @methodName : syncInfo
   * @date : 2025-03-13 오후 8:59
   * @author : wongil
   * @Description: 지수 정보 연동
   **/
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping("/index-infos")
  public Flux<SyncJobDto> syncInformation() {
    return syncJobsService.syncInfo();


  }

  /**
   * @methodName : syncData
   * @date : 2025-03-13 오후 9:00
   * @author : wongil
   * @Description: 지수 '데이터' 연동
   **/
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping("/index-data")
  public Flux<SyncJobDto> syncData(@RequestBody @Validated IndexDataSyncRequest request,
      HttpServletRequest httpRequest) {

    return syncJobsService.syncData(request, httpRequest);
  }


  /**
   * @methodName : findSyncJob
   * @date : 2025-03-13 오후 9:06
   * @author : wongil
   * @Description: 연동 작업 목록을 조회 jobType -> 연동 작업 목록(INDEX_INFO, INDEX_DATA) indexInfoId -> 지수 정보 ID
   * baseDateFrom -> 대상 날짜 부터 baseDateTo- -> 대상 날짜 까지 worker -> 작업자(IP, system) jobTimeFrom -> 작업 일시
   * 부터 jobTimeTo -> 작업 일시 까지 status -> 작업 상태(SUCCESS, FAILED) idAfter -> 이전 페이지의 마지막 요소 ID cursor
   * -> 다음 페이지 시작점(커서 페이징) sortedField -> 정렬 필드(targetDate, jobTime), default -> jobTime
   * sortDirection -> 정렬 방향, default -> desc size -> 페이지 크기, default -> 10
   **/
  @GetMapping
  public CursorPageResponseSyncJobDto findSyncJob(
      @RequestParam("jobType") ContentType jobType,
      @RequestParam("indexInfoId") Long indexInfoId,
      @RequestParam("baseDateFrom") LocalDate baseDateFrom,
      @RequestParam("baseDateTo") LocalDate baseDateTo,
      @RequestParam("worker") String worker,
      @RequestParam("jobTimeFrom") LocalDateTime jobTimeFrom,
      @RequestParam("jobTimeTo") LocalDateTime jobTimeTo,
      @RequestParam("status") String status,
      @RequestParam("idAfter") Long idAfter,
      @RequestParam("cursor") String cursor,
      @RequestParam("sortField") String sortField,
      @RequestParam("sortDirection") String sortDirection,
      @RequestParam("size") int size
  ) {

    return null;
  }

}
