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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;



@Service
@Transactional
@RequiredArgsConstructor
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
            .orElseThrow(() -> new NotFoundException("잘못된 요청입니다."));
    return indexMapper.toDto((index));
  }


  public CursorPageResponseIndexInfoDto<IndexInfoDto> getIndexInfos(
          String indexClassification, String indexName, Boolean favorite, String cursor, Long idAfter,
          String sortField, String sortDirection, int size, Pageable pageable) {

    // cursor와 idAfter를 동시에 처리하기 위한 로직
    Long cursorIdAfter = null;
    if (cursor != null) {
      cursorIdAfter = decodeCursor(cursor); // cursor를 디코딩하여 idAfter 값 추출
    }

    // idAfter와 cursor는 동시에 제공되지 않도록 처리하거나, cursor를 우선적으로 사용
    if (cursorIdAfter == null && idAfter != null) {
      cursorIdAfter = idAfter; // cursor가 없으면 idAfter 사용
    }

    // 정렬 조건 생성
    Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

    // 지수 분류명, 지수명, 즐겨찾기 조건을 반영하여 필터링
    List<Index> indexList;
    if (cursorIdAfter == null) {
      indexList = indexRepository.findByIndexClassificationAndIndexNameAndFavorite(
              indexClassification, indexName, favorite, pageable);
    } else {
      indexList = indexRepository.findByIdGreaterThanAndIndexClassificationAndIndexNameAndFavorite(
              cursorIdAfter, indexClassification, indexName, favorite, pageable);
    }

    // DTO 변환
    List<IndexInfoDto> indexDtos = indexList.stream()
            .map(indexMapper::toDto)
            .collect(Collectors.toList());

    // 마지막 인덱스 ID 계산 (커서 기반 페이지네이션을 위해 마지막 ID 확인)
    Long lastIndexId = indexDtos.isEmpty() ? null : indexDtos.get(indexDtos.size() - 1).id();

    // 다음 페이지 여부 판단
    boolean hasNext = indexDtos.size() == pageable.getPageSize();

    // 페이지네이션 응답 생성
    return new CursorPageResponseIndexInfoDto<>(
            indexDtos,
            hasNext ? String.valueOf(lastIndexId) : null,  // nextCursor
            lastIndexId,
            pageable.getPageSize(),
            indexRepository.count(),
            hasNext
    );
  }


  private String encodeCursor(Long id) {
    return Base64.getEncoder().encodeToString(id.toString().getBytes(StandardCharsets.UTF_8));
  }

  private Long decodeCursor(String cursor) {
    byte[] decodedBytes = Base64.getDecoder().decode(cursor);
    String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
    return Long.parseLong(decodedString);
  }

  public List<IndexInfoSummaryDto> getIndexSummaries() {
    return indexRepository.findAllProjectBy();
  }

}
