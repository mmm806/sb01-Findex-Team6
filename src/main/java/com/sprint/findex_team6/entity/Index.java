package com.sprint.findex_team6.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Index {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String indexClassification;  // 지수 분류명
  private String indexName;     // 지수명
  private Integer employedItemsCount; // 채용 종목 수
  private LocalDate baseDate;    // 기준 시점
  private BigDecimal baseIndex; // 기준 지수

  @Enumerated(EnumType.STRING)
  private SourceType sourceType;    // 소스 타입

  private Boolean favorite;   // 즐겨찾기
}
