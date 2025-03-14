package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.request.IndexInfoCreateRequest;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.mapper.IndexMapper;
import com.sprint.findex_team6.repository.IndexRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexService {
  private IndexMapper indexMapper;
  private IndexRepository indexRepository;

  public Index create(IndexInfoCreateRequest indexInfoCreateRequest){
    Long id = -1L;
    if(indexRepository.findByIndexName(indexInfoCreateRequest.indexName()).isEmpty()){
      return null;
    }else{
      id = indexRepository.findByIndexName(indexInfoCreateRequest.indexName()).get().getId();
    }

    String indexClassification = indexInfoCreateRequest.indexClassification();
    String indexName = indexInfoCreateRequest.indexName();
    LocalDate baseDate = indexInfoCreateRequest.basePointInTime();
    BigDecimal baseIndex =indexInfoCreateRequest.baseIndex();
    int employedItemsCount = indexInfoCreateRequest.employedItemsCount();
    boolean favorite = indexInfoCreateRequest.favorite();

    return new Index(id,indexClassification,indexName,employedItemsCount,baseDate,baseIndex,
        SourceType.OPEN_API,favorite);
  }
//
//  public Index createByUser(IndexInfoCreateRequest indexInfoCreateRequest){
//    Long id = -1L;
//    if(indexRepository.findByIndexName(indexInfoCreateRequest.indexName()).isEmpty()){
//      return null;
//    }else{
//      id = indexRepository.findByIndexName(indexInfoCreateRequest.indexName()).get().getId();
//    }
//
//    String indexClassification = indexInfoCreateRequest.indexClassification();
//    String indexName = indexInfoCreateRequest.indexName();
//    LocalDate baseDate = indexInfoCreateRequest.basePointInTime();
//    BigDecimal baseIndex =indexInfoCreateRequest.baseIndex();
//    int employedItemsCount = indexInfoCreateRequest.employedItemsCount();
//    boolean favorite = indexInfoCreateRequest.favorite();
//
//    return new Index(id,indexClassification,indexName,employedItemsCount,baseDate,baseIndex,
//        SourceType.USER,favorite);
//  }
//
//  private Index makeIndex()

}
