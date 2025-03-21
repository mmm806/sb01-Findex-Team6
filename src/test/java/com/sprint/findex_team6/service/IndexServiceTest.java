package com.sprint.findex_team6.service;

import static org.junit.jupiter.api.Assertions.*;

import com.sprint.findex_team6.dto.request.IndexInfoCreateRequest;
import com.sprint.findex_team6.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.repository.IndexRepository;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

@SpringBootTest
class IndexServiceTest {

  private final IndexService indexService;

  @Autowired
  public IndexServiceTest(IndexService indexService) {
    this.indexService = indexService;
  }

  @Test
  void create() {
    IndexInfoCreateRequest request = new IndexInfoCreateRequest("aa","name",20, LocalDate.now(), BigDecimal.ONE,true);
    indexService.create(request);
  }

  @Test
  void update() {
    IndexInfoUpdateRequest updateRequest = new IndexInfoUpdateRequest(300,LocalDate.now(),BigDecimal.TEN,false);
    indexService.update(updateRequest,12L);
  }

  @Test
  void delete() {
    indexService.delete(12L);
  }
}