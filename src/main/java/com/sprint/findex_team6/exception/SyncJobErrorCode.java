package com.sprint.findex_team6.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SyncJobErrorCode {
  INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "날짜 형식은 yyMMdd입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 요청입니다.", "서버 오류");

  private final HttpStatus status;
  private final String message;
  private final String details;

  SyncJobErrorCode(HttpStatus status, String message, String details) {
    this.status = status;
    this.message = message;
    this.details = details;
  }
}
