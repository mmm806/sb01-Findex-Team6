package com.sprint.findex_team6.exception.syncjobs;

import com.sprint.findex_team6.controller.SyncJobsController;
import com.sprint.findex_team6.dto.response.ErrorResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = {SyncJobsController.class})
public class SyncDataJobsServiceExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(DateTimeParseException.class)
  public ErrorResponse dateTimeParseException(DateTimeParseException e) {
    SyncJobErrorCode errorCode = SyncJobErrorCode.INVALID_DATE_FORMAT;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus().value(),
        errorCode.getMessage(), errorCode.getDetails());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(DuplicateIndexException.class)
  public ErrorResponse duplicatedIndex(DuplicateIndexException e) {
    SyncJobErrorCode errorCode = SyncJobErrorCode.DUPLICATE_INDEX_ID;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus()
        .value(), errorCode.getMessage(), errorCode.getDetails());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(SyncFailedException.class)
  public ErrorResponse syncInfoFailed(SyncFailedException e) {
    SyncJobErrorCode errorCode = SyncJobErrorCode.FAILED_SYNC_INFO;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus()
        .value(), errorCode.getMessage(), errorCode.getDetails());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(FailedCallOpenApiException.class)
  public ErrorResponse callOpenApiFailed(FailedCallOpenApiException e) {
    SyncJobErrorCode errorCode = SyncJobErrorCode.FAILED_SYNC_INFO;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus()
        .value(), errorCode.getMessage(), errorCode.getDetails());
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundIndexException.class)
  public ErrorResponse notFoundIndex(NotFoundIndexException e) {
    SyncJobErrorCode errorCode = SyncJobErrorCode.FAILED_SYNC_INFO;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus()
        .value(), errorCode.getMessage(), errorCode.getDetails());
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundIndeValException.class)
  public ErrorResponse notFoundIndexVal(NotFoundIndeValException e) {
    SyncJobErrorCode errorCode = SyncJobErrorCode.FAILED_SYNC_INFO;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus()
        .value(), errorCode.getMessage(), errorCode.getDetails());
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundItemException.class)
  public ErrorResponse notFoundItem(NotFoundItemException e) {
    SyncJobErrorCode errorCode = SyncJobErrorCode.FAILED_SYNC_INFO;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus()
        .value(), errorCode.getMessage(), errorCode.getDetails());
  }

  /**
  * @methodName : globalException
  * @date : 2025-03-20 오후 8:04
  * @author : wongil
  * @Description: 위의 에러가 아니면 서버 에러
  **/
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ErrorResponse globalException(Exception e) {
    log.error("error: ", e);
    SyncJobErrorCode errorCode = SyncJobErrorCode.INTERNAL_SERVER_ERROR;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus().value(), errorCode.getMessage(), errorCode.getDetails());
  }
}
