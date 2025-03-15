package com.sprint.findex_team6.exception;

import com.sprint.findex_team6.controller.SyncJobsController;
import com.sprint.findex_team6.dto.response.ErrorResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {SyncJobsController.class})
public class SyncDataJobsServiceExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ErrorResponse dateTimeParseException(HttpMessageNotReadableException e) {
    SyncJobErrorCode errorCode = SyncJobErrorCode.INVALID_DATE_FORMAT;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus().value(), errorCode.getMessage(), errorCode.getDetails());
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ErrorResponse globalException(Exception e) {
    SyncJobErrorCode errorCode = SyncJobErrorCode.INTERNAL_SERVER_ERROR;

    return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus().value(), errorCode.getMessage(), errorCode.getDetails());
  }
}
