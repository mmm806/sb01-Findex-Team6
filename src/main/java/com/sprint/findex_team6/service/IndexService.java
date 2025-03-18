package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.IndexInfoSummaryDto;
import com.sprint.findex_team6.dto.request.IndexInfoCreateRequest;
import com.sprint.findex_team6.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex_team6.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex_team6.dto.response.ErrorResponse;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.exception.NotFoundException;
import com.sprint.findex_team6.mapper.IndexMapper;
import com.sprint.findex_team6.repository.IndexRepository;
import jakarta.transaction.Transactional;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexService {
  private final IndexMapper indexMapper;
  private final IndexRepository indexRepository;

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

  public IndexInfoDto getIndexInfoById(Long id) {
    Index index = indexRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("조회한 지수 정보를 찾을 수 없습니다."));
    return indexMapper.toDto((index));
  }


  public CursorPageResponseIndexInfoDto<IndexInfoDto> getIndexInfos(
          String indexClassification, String indexName, Boolean favorite, String sortField, String sortDirection,
          Long idAfter, Pageable pageable) {

    Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

    List<Index> indexList = (idAfter == null)
            ? indexRepository.findAll(pageable).getContent()
            : indexRepository.findByIdGreaterThan(idAfter, pageable);

    List<IndexInfoDto> indexDtos = indexList.stream()
            .map(indexMapper::toDto)
            .toList();

    return new CursorPageResponseIndexInfoDto<>(
            indexDtos,
            indexDtos.isEmpty() ? null : String.valueOf(indexDtos.get(indexDtos.size() - 1).id()),
            indexDtos.isEmpty() ? null : indexDtos.get(indexDtos.size() - 1).id(),
            pageable.getPageSize(),
            indexRepository.count(),
            !indexDtos.isEmpty()
    );
  }


  public List<IndexInfoSummaryDto> getIndexSummaries() {
    return indexRepository.findAllProjectBy();
  }

}
