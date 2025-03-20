package com.sprint.findex_team6.service;


import com.sprint.findex_team6.entity.AutoIntegration;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.repository.AutoIntegrationRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AutoIntegrationService {

  private final AutoIntegrationRepository autoIntegrationRepository;


  public ResponseEntity<?> save(Index index, Boolean enabled){
    AutoIntegration autoIntegration = new AutoIntegration(index,enabled);
    autoIntegration.setUpdateDate(LocalDate.now());

    try{
      autoIntegrationRepository.save(autoIntegration);
    } catch (Exception e){
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    return ResponseEntity.status(HttpStatus.CREATED).body(autoIntegration);
  }

  public ResponseEntity<?> update(Index index){
    if(autoIntegrationRepository.findByIndex(index).isEmpty()){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    AutoIntegration autoIntegration = autoIntegrationRepository.findByIndex(index).get();
    if(autoIntegration.getEnabled()) autoIntegration.setEnabled(false);
    else autoIntegration.setEnabled(true);

    autoIntegrationRepository.save(autoIntegration);

    return ResponseEntity.status(HttpStatus.CREATED).body(autoIntegration);
  }


  @Scheduled(cron = "0 0 0 * * *") // 밤 12시에 연동
  public ResponseEntity<?> updateAuto(){
    List<AutoIntegration> integrations = findActiveIndices();

    for(AutoIntegration autoIntegration : integrations){
      LocalDate currUpdate = LocalDate.now();
      autoIntegration.setUpdateDate(currUpdate);

      autoIntegrationRepository.save(autoIntegration);
    }

    return ResponseEntity.status(HttpStatus.OK).build();
  }

  private List<AutoIntegration> findActiveIndices(){
    List<AutoIntegration> list = new ArrayList<>();

    List<AutoIntegration> autoIntegrations = autoIntegrationRepository.findAll();
    for(AutoIntegration autoIntegration : autoIntegrations){
      if(autoIntegration.getEnabled()){
        list.add(autoIntegration);
      }
    }

    return list;
  }

}
