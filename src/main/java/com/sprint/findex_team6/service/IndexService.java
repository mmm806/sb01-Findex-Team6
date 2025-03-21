package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.request.IndexInfoCreateRequest;
import com.sprint.findex_team6.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex_team6.dto.response.ErrorResponse;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.mapper.IndexMapper;
import com.sprint.findex_team6.repository.IndexRepository;
import jakarta.transaction.Transactional;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@Transactional
@RequiredArgsConstructor
public class IndexService {
  private final IndexMapper indexMapper;
  private final IndexRepository indexRepository;
  private final AutoIntegrationService autoIntegrationService;

  public ResponseEntity<?> create(IndexInfoCreateRequest indexInfoCreateRequest){

   if(hasNullFields(indexInfoCreateRequest)){
     String checkNullField = checkNullField(indexInfoCreateRequest) + "는 필수입니다.";
     ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(),HttpStatus.BAD_REQUEST.value(),"잘못된 요청입니다.", checkNullField);
     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
   }

    String indexClassification = indexInfoCreateRequest.indexClassification();
    String indexName = indexInfoCreateRequest.indexName();
    LocalDate baseDate = indexInfoCreateRequest.basePointInTime();
    BigDecimal baseIndex =indexInfoCreateRequest.baseIndex();
    Integer employedItemsCount = indexInfoCreateRequest.employedItemsCount();
    Boolean favorite = indexInfoCreateRequest.favorite();

    Index index = new Index(indexClassification, indexName, employedItemsCount, baseDate, baseIndex,
        SourceType.USER, favorite);
    indexRepository.save(index);
    ResponseEntity<?> response= autoIntegrationService.save(index,false);
    if(response.getStatusCode().isSameCodeAs(HttpStatus.INTERNAL_SERVER_ERROR)){
      ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(),HttpStatus.BAD_REQUEST.value(),"서버 오류입니다.", "자동 연동에 실패하였습니다.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    return ResponseEntity.status(HttpStatus.CREATED).body(indexMapper.toDto(index));
  }

  public IndexInfoDto update(IndexInfoUpdateRequest request, Long id){
    Integer employedItemsCount = request.employedItemsCount();
    LocalDate basePointInTime = request.basePointInTime();
    BigDecimal baseIndex = request.baseIndex();
    Boolean favorite = request.favorite();

    Index index = null;
    if(indexRepository.findById(id).isPresent()){
      index = indexRepository.findById(id).get();
    }
    else{
      throw new NoSuchElementException();
    }

    if(employedItemsCount != null){
      index.setEmployedItemsCount(employedItemsCount);
    }

    if(basePointInTime != null){
      index.setBaseDate(basePointInTime);
    }

    if(baseIndex != null){
      index.setBaseIndex(baseIndex);
    }

    if(favorite != null){
      index.setFavorite(favorite);
    }

    indexRepository.save(index);

    return indexMapper.toDto(index);
  }

  public ResponseEntity<Void> delete(Long id){
    if(indexRepository.findById(id).isPresent()){
      autoIntegrationService.delete(id);
      indexRepository.deleteById(id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }

  private boolean hasNullFields(Object obj) {
    for (Field field : obj.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      try {
        if (field.get(obj) == null) {
          return true;
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  private String checkNullField(Object obj){
    for(Field field : obj.getClass().getDeclaredFields()){
      field.setAccessible(true);
      try {
        if (field.get(obj) == null) {
          return field.getName();
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

}
