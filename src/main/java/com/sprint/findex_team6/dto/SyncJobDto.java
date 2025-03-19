package com.sprint.findex_team6.dto;


import com.sprint.findex_team6.entity.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class SyncJobDto{
  @Setter
  Long id;

  ContentType jobType;

  @Setter
  Long indexInfoId;

  LocalDate targetDate;
  String worker;
  LocalDateTime jobTime;
  String result;
}
