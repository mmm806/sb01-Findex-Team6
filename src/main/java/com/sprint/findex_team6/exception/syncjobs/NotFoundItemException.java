package com.sprint.findex_team6.exception.syncjobs;

public class NotFoundItemException extends RuntimeException {

  public NotFoundItemException() {
  }

  public NotFoundItemException(String message) {
    super(message);
  }
}
