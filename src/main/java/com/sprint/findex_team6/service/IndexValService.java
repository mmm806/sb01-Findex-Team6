package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.IndexDataDto;
import com.sprint.findex_team6.dto.request.IndexDataCreateRequest;
import com.sprint.findex_team6.dto.request.IndexDataUpdateRequest;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.exception.NotFoundException;
import com.sprint.findex_team6.mapper.IndexValMapper;
import com.sprint.findex_team6.repository.IndexRepository;
import com.sprint.findex_team6.repository.IndexValRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexValService {

  private final IndexValRepository indexValRepository;
  private final IndexRepository indexRepository;
  private final IndexValMapper indexValMapper;

  @Transactional
  public IndexDataDto create(IndexDataCreateRequest request) {
    Index index = indexRepository.findById(request.indexInfoId())
        .orElseThrow(() -> new NotFoundException("Index not found. id=" + request.indexInfoId()));
    IndexVal indexVal = IndexVal.builder()
        .date(request.baseDate())
        .sourceType(SourceType.USER)
        .marketPrice(request.marketPrice())
        .closePrice(request.closingPrice())
        .highPrice(request.highPrice())
        .lowPrice(request.lowPrice())
        .versus(request.versus())
        .fluctuationRate(request.fluctuationRate())
        .tradingQuantity(request.tradingQuantity())
        .tradingPrice(BigDecimal.valueOf(request.tradingPrice()))
        .marketTotalCount(BigDecimal.valueOf(request.marketTotalAmount()))
        .index(index)
        .build();

    indexVal = indexValRepository.save(indexVal);
    return indexValMapper.toDto(indexVal);
  }

  @Transactional
  public IndexDataDto update(Long id, IndexDataUpdateRequest request) {
    IndexVal indexVal = indexValRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("IndexVal not found. id=" + id));
    indexVal.setMarketPrice(request.marketPrice());
    indexVal.setClosePrice(request.closingPrice());
    indexVal.setHighPrice(request.highPrice());
    indexVal.setLowPrice(request.lowPrice());
    indexVal.setVersus(request.versus());
    indexVal.setFluctuationRate(request.fluctuationRate());
    indexVal.setTradingQuantity(request.tradingQuantity());
    indexVal.setTradingPrice(BigDecimal.valueOf(request.tradingPrice()));
    indexVal.setMarketTotalCount(BigDecimal.valueOf(request.marketTotalAmount()));
    return indexValMapper.toDto(indexVal);
  }


  @Transactional
  public void delete(Long id) {
    indexValRepository.deleteById(id);
  }


}
