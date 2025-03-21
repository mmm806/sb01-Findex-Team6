package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.CursorPageResponse;
import com.sprint.findex_team6.dto.IndexDataDto;
import com.sprint.findex_team6.dto.request.IndexDataCreateRequest;
import com.sprint.findex_team6.dto.request.IndexDataQueryRequest;
import com.sprint.findex_team6.dto.request.IndexDataUpdateRequest;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.exception.NotFoundException;
import com.sprint.findex_team6.mapper.CursorPageResponseMapper;
import com.sprint.findex_team6.mapper.IndexValMapper;
import com.sprint.findex_team6.repository.IndexRepository;
import com.sprint.findex_team6.repository.IndexValRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexValService {

  private final IndexValRepository indexValRepository;
  private final IndexRepository indexRepository;
  private final IndexValMapper indexValMapper;
  private final CursorPageResponseMapper cursorPageResponseMapper;

  @Transactional
  public IndexDataDto create(IndexDataCreateRequest request) {
    Index index = indexRepository.findById(request.indexInfoId())
        .orElseThrow(() -> new NotFoundException("Index not found. id=" + request.indexInfoId()));
    IndexVal indexVal = IndexVal.builder()
        .baseDate(request.baseDate())
        .sourceType(SourceType.USER)
        .marketPrice(request.marketPrice())
        .closingPrice(request.closingPrice())
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

  public CursorPageResponse<IndexDataDto> findIndexData(IndexDataQueryRequest request, Pageable pageable) {
    //값 세팅
    Long indexInfoId = request.indexInfoId();
    LocalDate startDate = request.startDate() == null ?
        LocalDate.of(1900, 1, 1) : request.startDate();
    LocalDate endDate = request.endDate() == null ?
        LocalDate.now() : request.endDate();
    String cursor = request.cursor();
    Long idAfter = request.idAfter();

    //indexInfoId가 null이면 indexInfoId에 관계 없이 최신 데이터 리턴
    if (indexInfoId == null) {
      Page<IndexDataDto> page = indexValRepository.findAll(pageable)
          .map(indexValMapper::toDto);
      return cursorPageResponseMapper.fromPageIndexDataDto(page);
    }

    Sort sort = pageable.getSort();
    Order order = sort.iterator().next();
    String property = order.getProperty();
    Page<IndexVal> page;

    if (cursor == null) { //cursor가 null 이면 startDate-endDate 사이의 데이터 리턴
      page = indexValRepository.findByIndex_IdAndBaseDateBetween(
          indexInfoId, startDate, endDate, pageable);
    }
    else if (property.equals("closingPrice")) { //정렬 필드가 closingPrice인 경우
      if (order.getDirection().isDescending()) { //내림차순이면 cursor보다 작은 데이터를 찾음
        page = indexValRepository.findByClosingPriceCursorDesc(
            indexInfoId, startDate, endDate, new BigDecimal(cursor), idAfter, pageable);
      } else { //오름차순이면 cursor보다 큰 데이터를 찾음
        page = indexValRepository.findByClosingPriceCursorAsc(
            indexInfoId, startDate, endDate, new BigDecimal(cursor), idAfter, pageable);
      }
    } else { //정렬 필드가 baseDate인 경우
      if (order.getDirection().isDescending()) { //위와 같이 내림차순, 오름차순 처리
        page = indexValRepository.findByBaseDateCursorDesc(
            indexInfoId, startDate, endDate, LocalDate.parse(cursor), idAfter, pageable);
      } else {
        page = indexValRepository.findByBaseDateCursorAsc(
            indexInfoId, startDate, endDate, LocalDate.parse(cursor), idAfter, pageable);
      }
    }
    return cursorPageResponseMapper.fromPageIndexDataDto(page.map(indexValMapper::toDto));
  }

  @Transactional
  public IndexDataDto update(Long id, IndexDataUpdateRequest request) {
    IndexVal indexVal = indexValRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("IndexVal not found. id=" + id));
    if (request.marketPrice() != null) {
      indexVal.setMarketPrice(request.marketPrice());
    }
    if (request.closingPrice() != null) {
      indexVal.setClosingPrice(request.closingPrice());
    }
    if (request.highPrice() != null) {
      indexVal.setHighPrice(request.highPrice());
    }
    if (request.lowPrice() != null) {
      indexVal.setLowPrice(request.lowPrice());
    }
    if (request.versus() != null) {
      indexVal.setVersus(request.versus());
    }
    if (request.fluctuationRate() != null) {
      indexVal.setFluctuationRate(request.fluctuationRate());
    }
    if (request.tradingQuantity() != null) {
      indexVal.setTradingQuantity(request.tradingQuantity());
    }
    if (request.tradingPrice() != null) {
      indexVal.setTradingPrice(BigDecimal.valueOf(request.tradingPrice()));
    }
    if (request.marketTotalAmount() != null) {
      indexVal.setMarketTotalCount(BigDecimal.valueOf(request.marketTotalAmount()));
    }
    return indexValMapper.toDto(indexVal);
  }

  @Transactional
  public void delete(Long id) {
    indexValRepository.deleteById(id);
  }
}
