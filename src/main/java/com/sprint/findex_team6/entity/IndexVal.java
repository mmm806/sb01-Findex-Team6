package com.sprint.findex_team6.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "index_val", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"base_date", "index_id"})
})
public class IndexVal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "base_date")
  private LocalDate baseDate;         // 기준 일자

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", columnDefinition = "source_type")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private SourceType sourceType;      // 소스 타입

  private BigDecimal marketPrice; // 시가
  private BigDecimal closingPrice; // 종가
  private BigDecimal highPrice; // 고가
  private BigDecimal lowPrice;  // 저가
  private BigDecimal versus;       // 대비
  private BigDecimal fluctuationRate; // 등락률
  private Long tradingQuantity;             // 거래량
  private BigDecimal tradingPrice; // 거래대금
  private BigDecimal marketTotalAmount;    // 상장 시가 총액

  @ManyToOne //하나의 지수정보에 대해 여러개의 지수 데이터
  @JoinColumn(name = "index_id")
  private Index index;

  @Builder
  public IndexVal(
      LocalDate baseDate,
      SourceType sourceType,
      BigDecimal marketPrice,
      BigDecimal closingPrice,
      BigDecimal highPrice,
      BigDecimal lowPrice,
      BigDecimal versus,
      BigDecimal fluctuationRate,
      Long tradingQuantity,
      BigDecimal tradingPrice,
      BigDecimal marketTotalAmount,
      Index index
  ) {
    this.baseDate = baseDate;
    this.sourceType = sourceType;
    this.marketPrice = marketPrice;
    this.closingPrice = closingPrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.versus = versus;
    this.fluctuationRate = fluctuationRate;
    this.tradingQuantity = tradingQuantity;
    this.tradingPrice = tradingPrice;
    this.marketTotalAmount = marketTotalAmount;
    this.index = index;
  }

  /**
  * @methodName : changeData
  * @date : 2025-03-19 오후 2:33
  * @author : wongil
  * @Description: IndexVal 거래와 관련된 데이터를 설정하기 위한 메서드
  **/
  public IndexVal changeData(Double mkp, Double clpr, Double hipr, Double lopr, Double vs, Double fltRt,
      Long trqu,
      Double trPrc,
      Double lstgMrktTotAmt) {

    this.marketPrice = BigDecimal.valueOf(mkp);
    this.closingPrice = BigDecimal.valueOf(clpr);
    this.highPrice = BigDecimal.valueOf(hipr);
    this.lowPrice = BigDecimal.valueOf(lopr);
    this.versus = BigDecimal.valueOf(vs);
    this.fluctuationRate = BigDecimal.valueOf(fltRt);
    this.tradingQuantity = trqu;
    this.tradingPrice = BigDecimal.valueOf(trPrc);
    this.marketTotalAmount = BigDecimal.valueOf(lstgMrktTotAmt);


    return this;
  }
}
