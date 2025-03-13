package com.sprint.findex_team6.mapper;


import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.entity.ConnectType;
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
    Index index = new Index(1L,
        "KOSPI",
        "KOSPI",
        3,
        LocalDate.of(1980, 1, 1),
        BigDecimal.ONE, SourceType.USER,
        false);
    IndexDataLink indexDataLink = new IndexDataLink(
        2L,
        ConnectType.IDX_INFO,
        LocalDateTime.now(),
        "test",
        LocalDateTime.now(),
        true,
        index
    );

    IndexDateLinkMapper mapper = Mappers.getMapper(IndexDateLinkMapper.class);
    SyncJobDto dto = mapper.toDto(indexDataLink);

    assertThat(dto.id()).isEqualTo(indexDataLink.getId());
    assertThat(dto.jobType()).isEqualTo(indexDataLink.getSourceType());
    assertThat(dto.indexInfoId()).isEqualTo(indexDataLink.getIndex().getId());
    assertThat(dto.targetDate()).isEqualTo(indexDataLink.getTargetDate());
    assertThat(dto.worker()).isEqualTo(indexDataLink.getWorker());
    assertThat(dto.jobTime()).isEqualTo(indexDataLink.getJobTime());
    assertThat(dto.result()).isEqualTo(indexDataLink.getResult());

  }
}