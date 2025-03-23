package com.sprint.findex_team6.exception.syncjobs;

public class FailedCallOpenApiException extends RuntimeException {

  public FailedCallOpenApiException() {
  }

  public FailedCallOpenApiException(String message) {
    super(message);
  }
}
