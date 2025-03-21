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
import com.sprint.findex_team6.mapper.CursorPageResponseMapper;
import com.sprint.findex_team6.mapper.IndexMapper;
import com.sprint.findex_team6.repository.IndexRepository;
import jakarta.transaction.Transactional;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;



@Service
@Transactional
@RequiredArgsConstructor
public class IndexService {
  private final IndexMapper indexMapper;
  private final IndexRepository indexRepository;
  private final CursorPageResponseMapper cursorPageResponseMapper;


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
            .orElseThrow(() -> new NotFoundException("잘못된 요청입니다."));
    return indexMapper.toDto((index));
  }


  public CursorPageResponseIndexInfoDto<IndexInfoDto> getIndexInfos(
          String indexClassification, String indexName, Boolean favorite,
          String cursor, Long idAfter,
          String sortField, String sortDirection,
          int size, Pageable pageable) {

    // 기본값 처리
    indexClassification = indexClassification == null ? "" : indexClassification;
    indexName = indexName == null ? "" : indexName;
    favorite = favorite == null ? false : favorite;

    Long cursorIdAfter = null;
    if (cursor != null) {
      cursorIdAfter = Long.parseLong(cursor);
    }
    if (cursorIdAfter == null && idAfter != null) {
      cursorIdAfter = idAfter;
    }

    // 정렬 기준 설정
    Sort sort = Sort.by(Sort.Order.by(sortField));
    if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
      sort = Sort.by(Sort.Order.desc(sortField));
    } else {
      sort = Sort.by(Sort.Order.asc(sortField));
    }

    // Pageable 생성 (size와 정렬 포함)
    Pageable customPageable = PageRequest.of(pageable.getPageNumber(), size, sort);

    List<Index> indexList = null;

    // 커서가 없을 경우 일반 필터 조건만 적용
    if (cursor == null) {
      indexList = indexRepository.findByIndexClassificationAndIndexNameAndFavorite(
              indexClassification, indexName, favorite, customPageable);
    }
    // 정렬 기준이 indexClassification일 때
    else if (sortField.equals("indexClassification")) {
      if (sort.isSorted() && sort.iterator().next().getDirection().isDescending()) {
        indexList = indexRepository.findByIndexClassificationCursorDesc(
                cursor, customPageable);
      } else {
        indexList = indexRepository.findByIndexClassificationCursorAsc(
                cursor, customPageable);
      }
    }
    // 정렬 기준이 indexName일 때
    else if (sortField.equals("indexName")) {
      if (sort.isSorted() && sort.iterator().next().getDirection().isDescending()) {
        indexList = indexRepository.findByIndexNameCursorDesc(
                cursor, customPageable);
      } else {
        indexList = indexRepository.findByIndexNameCursorAsc(
                cursor, customPageable);
      }
    }

    Page<Index> page = new PageImpl<>(indexList, customPageable, indexList.size());

    // Page<Index>를 Page<IndexInfoDto>로 변환
    Page<IndexInfoDto> dtoPage = page.map(indexMapper::toDto);

    // Page<IndexInfoDto>를 커서 기반 페이지 응답으로 변환
    return cursorPageResponseMapper.fromPageIndexInfoDto(dtoPage);
  }





  public List<IndexInfoSummaryDto> getIndexSummaries() {
    // Index 리스트를 가져온 후, DTO로 변환
    List<Index> indexList = indexRepository.findAll();
    return indexList.stream()
            .map(indexMapper::toSummaryDto)  // Index -> IndexInfoSummaryDto 변환
            .collect(Collectors.toList());
  }

}
