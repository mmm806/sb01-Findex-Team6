package com.sprint.findex_team6.dto.request;

/*
요청 데이터가 소문자로 들어와서 부득이하게 변수 이름을 소문자로 맞췄습니다.
대문자로 하면 변환 과정을 거쳐야 하기 때문에 그냥 소문자로 처리하는 것이 더 좋다고 생각했습니다.
 */
public enum IndexDataSortField {
  baseDate, marketPrice, closingPrice, highPrice,
  lowPrice, versus, fluctuationRate, tradingQuantity,
  tradingPrice, marketTotalAmount
}
