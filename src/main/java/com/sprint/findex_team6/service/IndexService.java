package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.CursorPageResponse;
import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.IndexInfoSummaryDto;
import com.sprint.findex_team6.dto.request.*;
import com.sprint.findex_team6.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex_team6.dto.response.ErrorResponse;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.exception.NotFoundException;
import com.sprint.findex_team6.mapper.CursorPageResponseMapper;
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

  public IndexInfoDto getIndexInfoById(Long id) {
    Index index = indexRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("잘못된 요청입니다."));
    return indexMapper.toDto((index));
  }


  public CursorPageResponse<IndexInfoDto> getIndexInfos(
          IndexInfoQueryRequest request,
          PageRequest pageRequest,
          IndexSortField sortField,
          SortDirection sortDirection
  ) {
    String indexClassification = request.indexClassification();
    String indexName = request.indexName();
    Boolean favorite = request.favorite();
    String cursor = request.cursor();
    Long idAfter = request.idAfter();

    Sort sort = pageRequest.getSort();
    Sort.Order order = sort.iterator().next();
    boolean isDesc = order.getDirection().isDescending();

    Page<Index> page;

    // 커서가 없을 때는 일반 조건 검색
    if (cursor == null) {
      page = indexRepository.findAllByConditions(indexClassification, indexName, favorite, pageRequest);
    } else {
      if (sortField == IndexSortField.indexClassification) {
        page = isDesc
                ? indexRepository.findByIndexClassificationCursorDesc(indexClassification, indexName, favorite, cursor, idAfter, pageRequest)
                : indexRepository.findByIndexClassificationCursorAsc(indexClassification, indexName, favorite, cursor, idAfter, pageRequest);
      } else if (sortField == IndexSortField.indexName) {
        page = isDesc
                ? indexRepository.findByIndexNameCursorDesc(indexClassification, indexName, favorite, cursor, idAfter, pageRequest)
                : indexRepository.findByIndexNameCursorAsc(indexClassification, indexName, favorite, cursor, idAfter, pageRequest);
      } else {
        throw new IllegalArgumentException("지원하지 않는 정렬 필드입니다.");
      }
    }

    Page<IndexInfoDto> dtoPage = page.map(indexMapper::toDto);
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
