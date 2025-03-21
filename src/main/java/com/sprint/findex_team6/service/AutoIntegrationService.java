package com.sprint.findex_team6.service;


import com.sprint.findex_team6.entity.AutoIntegration;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.repository.AutoIntegrationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AutoIntegrationService {

  private final AutoIntegrationRepository autoIntegrationRepository;


  public ResponseEntity<?> save(Index index, Boolean enabled){
    AutoIntegration autoIntegration = new AutoIntegration(index,enabled);

    try{
      autoIntegrationRepository.save(autoIntegration);
    } catch (Exception e){
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    return ResponseEntity.status(HttpStatus.CREATED).body(autoIntegration);
  }

  public void delete(Long indexId){
    autoIntegrationRepository.deleteByIndex_Id(indexId);
  }

}
