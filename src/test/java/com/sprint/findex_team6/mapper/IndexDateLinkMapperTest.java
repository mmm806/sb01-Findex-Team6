package com.sprint.findex_team6.mapper;


import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.entity.ContentType;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexDataLink;
import com.sprint.findex_team6.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class IndexDateLinkMapperTest {

  @Test
  void toDto() {
    Index index = new Index(
        "KOSPI",
        "KOSPI",
        3,
        LocalDate.of(1980, 1, 1),
        BigDecimal.ONE, SourceType.USER,
        false);
    IndexDataLink indexDataLink = new IndexDataLink(
        2L,
        ContentType.INDEX_INFO,
        LocalDate.now(),
        "test",
        LocalDateTime.now(),
        true,
        index
    );

    IndexDateLinkMapper mapper = Mappers.getMapper(IndexDateLinkMapper.class);
    SyncJobDto dto = mapper.toDto(indexDataLink);

    assertThat(dto.getId()).isEqualTo(indexDataLink.getId());
    assertThat(dto.getJobType()).isEqualTo(indexDataLink.getSourceType());
    assertThat(dto.getIndexInfoId()).isEqualTo(indexDataLink.getIndex().getId());
    assertThat(dto.getTargetDate()).isEqualTo(indexDataLink.getTargetDate());
    assertThat(dto.getWorker()).isEqualTo(indexDataLink.getWorker());
    assertThat(dto.getJobTime()).isEqualTo(indexDataLink.getJobTime());
    assertThat(dto.getResult()).isEqualTo(indexDataLink.getResult());

  }
}