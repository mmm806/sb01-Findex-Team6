package com.sprint.findex_team6.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AutoSyncConfigDto {
  Long id;
  Long indexInfoId;
  String indexClassification;
  String indexName;
  boolean enabled;

  @QueryProjection
  public AutoSyncConfigDto(Long id, Long indexInfoId, String indexClassification, String indexName,
      boolean enabled) {
    this.id = id;
    this.indexInfoId = indexInfoId;
    this.indexClassification = indexClassification;
    this.indexName = indexName;
    this.enabled = enabled;
  }
}
