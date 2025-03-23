package com.sprint.findex_team6.exception.syncjobs;

public class DuplicateIndexException extends RuntimeException {

  public DuplicateIndexException() {
  }

  public DuplicateIndexException(String message) {
    super(message);
  }
}
