package com.sprint.findex_team6.dto.request;

import com.sprint.findex_team6.entity.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
* @package : com.sprint.findex_team6.dto.request
* @name : CursorPageRequest.java
* @date : 2025-03-17 오후 1:04
* @author : wongil
* @Description:
 * jobType -> 연동 작업 목록(INDEX_INFO, INDEX_DATA)
 * indexInfoId -> 지수 정보 ID
 * baseDateFrom -> 대상 날짜 부터
 * baseDateTo- -> 대상 날짜 까지
 * worker -> 작업자(IP, system)
 * jobTimeFrom -> 작업 일시부터
 * jobTimeTo -> 작업 일시 까지
 * status -> 작업 상태(SUCCESS, FAILED)
 * idAfter -> 이전 페이지의 마지막 요소 ID
 * cursor -> 다음 페이지 시작점(커서 페이징)
 * sortedField -> 정렬 필드(targetDate, jobTime), default -> jobTime
 * sortDirection -> 정렬 방향, default -> desc
 * size -> 페이지 크기, default -> 10
**/
@Data
public class CursorPageRequest {

  ContentType jobType;
  Long indexInfoId;
  LocalDate baseDateFrom;
  LocalDate baseDateTo;
  String worker;
  LocalDateTime jobTimeFrom;
  LocalDateTime jobTimeTo;
  String status;
  Long idAfter;
  String cursor;
  String sortField;
  String sortDirection;
  Integer size;

}
