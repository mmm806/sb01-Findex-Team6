package com.sprint.findex_team6.exception.syncjobs;

public class NotFoundIndexException extends RuntimeException {

  public NotFoundIndexException() {
  }

  public NotFoundIndexException(String message) {
    super(message);
  }
}
