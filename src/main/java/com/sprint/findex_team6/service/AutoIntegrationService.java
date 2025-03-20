package com.sprint.findex_team6.service;


import com.sprint.findex_team6.repository.AutoIntegrationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AutoIntegrationService {

  private final AutoIntegrationRepository autoIntegrationRepository;

  public ResponseEntity<?> save(Long indexId){
    
  }

}
