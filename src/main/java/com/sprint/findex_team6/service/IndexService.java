package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.request.IndexInfoCreateRequest;
import com.sprint.findex_team6.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.mapper.IndexMapper;
import com.sprint.findex_team6.repository.IndexRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


//TODO : null이 들어오는 경우 오류 반환
@Service
@RequiredArgsConstructor
public class IndexService {
  private IndexMapper indexMapper;
  private IndexRepository indexRepository;

  public IndexInfoDto create(IndexInfoCreateRequest indexInfoCreateRequest){
    String indexClassification = indexInfoCreateRequest.indexClassification();
    String indexName = indexInfoCreateRequest.indexName();
    LocalDate baseDate = indexInfoCreateRequest.basePointInTime();
    BigDecimal baseIndex =indexInfoCreateRequest.baseIndex();
    Integer employedItemsCount = indexInfoCreateRequest.employedItemsCount();
    Boolean favorite = indexInfoCreateRequest.favorite();

    Index index = new Index(indexClassification, indexName, employedItemsCount, baseDate, baseIndex,
        SourceType.USER, favorite);
    indexRepository.save(index);

    return indexMapper.toDto(index);
  }

  public IndexInfoDto update(IndexInfoUpdateRequest request, Long id){
    Integer employedItemsCount = request.employedItemsCount();
    LocalDate basePointInTime = request.basePointInTime();
    BigDecimal baseIndex = request.baseIndex();
    boolean favorite = request.favorite();

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

    index.setBaseIndex(baseIndex);
    index.setFavorite(favorite);

    indexRepository.save(index);

    return indexMapper.toDto(index);
  }

  public ResponseEntity<Void> delete(Long id){
    if(indexRepository.findById(id).isPresent()){
      indexRepository.deleteById(id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }

}
