package com.sprint.findex_team6.exception.syncjobs;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SyncJobErrorCode {
  INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "날짜 형식 오류"),
  DUPLICATE_INDEX_ID(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "지수 중복"),
  NOT_FOUND_INDEX(HttpStatus.BAD_REQUEST, "지수를 찾을 수 없습니다.", "지수 찾기 실패"),
  NOT_FOUND_INDEX_VAL(HttpStatus.BAD_REQUEST, "지수 데이터를 찾을 수 없습니다.", "지수 데이터 찾기 실패"),
  NOT_FOUND_ITEM(HttpStatus.BAD_REQUEST, "OPEN API 데이터를 찾을 수 없습니다.", "OPEN API 오류"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 요청입니다.", "서버 오류"),
  FAILED_SYNC_INFO(HttpStatus.BAD_REQUEST, "연동에 실패했습니다.", "지수 정보 연동 실패"),
  FAILED_CALL_API(HttpStatus.BAD_REQUEST, "API 호출에 실패했습니다.", "OPEN API 오류");

  private final HttpStatus status;
  private final String message;
  private final String details;

  SyncJobErrorCode(HttpStatus status, String message, String details) {
    this.status = status;
    this.message = message;
    this.details = details;
  }
}
