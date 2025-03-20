package com.sprint.findex_team6.error;

public class CustomException extends RuntimeException{
  private final ErrorCode errorCode;
  public CustomException(ErrorCode errorCode){
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
