package com.sprint.findex_team6.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.net.ssl.SSLSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndexVal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate baseDate;         // 기준 일자

  @Enumerated(EnumType.STRING)
  private SourceType sourceType;      // 소스 타입

  private BigDecimal marketPrice; // 시가
  private BigDecimal closingPrice; // 종가
  private BigDecimal highPrice; // 고가
  private BigDecimal lowPrice;  // 저가
  private BigDecimal versus;       // 대비
  private BigDecimal fluctuationRate; // 등락률
  private Long tradingQuantity;             // 거래량
  private BigDecimal tradingPrice; // 거래대금
  private BigDecimal marketTotalCount;    // 상장 시가 총액


  @ManyToOne //하나의 지수정보에 대해 여러개의 지수 데이터
  private Index index;
}
