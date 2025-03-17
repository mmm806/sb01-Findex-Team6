package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.IndexDataSyncRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@SpringBootTest
@Transactional
class SyncDataJobsServiceTest {

  @Autowired
  private SyncDataJobsService syncDataJobsService;

  @Test
  @DisplayName("지수 데이터 연동")
  void syncData() {
    LocalDate now = LocalDate.now();

    LocalDate localDate = LocalDate.of(2024, 07, 31);

    IndexDataSyncRequest request = new IndexDataSyncRequest(new ArrayList<Integer>(List.of(1,2,3)), localDate, LocalDate.now());
    System.out.println("request = " + request);

    Flux<SyncJobDto> syncJobDtoFlux = syncDataJobsService.syncData(request, null);

    List<SyncJobDto> syncJobDtos = syncJobDtoFlux.collectList().block();

    for (SyncJobDto syncJobDto : syncJobDtos) {
      System.out.println("syncJobDto = " + syncJobDto);
    }
  }
}