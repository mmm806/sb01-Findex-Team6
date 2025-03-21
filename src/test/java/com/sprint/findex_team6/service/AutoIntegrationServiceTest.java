package com.sprint.findex_team6.service;

import static org.junit.jupiter.api.Assertions.*;

import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.repository.IndexRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;


@SpringBootTest
@Transactional
class AutoIntegrationServiceTest {

  private final AutoIntegrationService autoIntegrationService;
  private final IndexRepository indexRepository;

  @Autowired
  public AutoIntegrationServiceTest(AutoIntegrationService autoIntegrationService,
      IndexRepository indexRepository) {
    this.autoIntegrationService = autoIntegrationService;
    this.indexRepository = indexRepository;
  }

  @Test
  void save() {
    Index index = indexRepository.findById(13L).get();

    ResponseEntity<?> response = autoIntegrationService.save(index,true);
    System.out.println(response.getStatusCode());

  }

  @Test
  void update() {
    Index index = indexRepository.findById(13L).get();

    ResponseEntity<?> response = autoIntegrationService.update(index);
    System.out.println(response.getStatusCode());
  }
}