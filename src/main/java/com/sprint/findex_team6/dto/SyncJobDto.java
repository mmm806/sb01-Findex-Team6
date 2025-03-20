package com.sprint.findex_team6.dto;


import com.querydsl.core.annotations.QueryProjection;
import com.sprint.findex_team6.entity.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class SyncJobDto {

  @Setter
  Long id;

  ContentType jobType;

  @Setter
  Long indexInfoId;

  LocalDate targetDate;
  String worker;
  LocalDateTime jobTime;
  String result;

  @QueryProjection
  public SyncJobDto(Long id, ContentType jobType, Long indexInfoId, LocalDate targetDate,
      String worker, LocalDateTime jobTime, String result) {
    this.id = id;
    this.jobType = jobType;
    this.indexInfoId = indexInfoId;
    this.targetDate = targetDate;
    this.worker = worker;
    this.jobTime = jobTime;
    this.result = result;
  }
}
