package com.sprint.findex_team6.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "index")
public class Index {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "index_classification")
  private String indexClassification;  // 지수 분류명

  @Column(name = "index_name")
  private String indexName;     // 지수명

  @Column(name = "employed_items_count")
  private Integer employedItemsCount; // 채용 종목 수

  @Column(name = "base_date")
  private LocalDate baseDate;    // 기준 시점

  @Column(name = "base_index")
  private BigDecimal baseIndex; // 기준 지수

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type")
  private SourceType sourceType;    // 소스 타입

  @Column
  private Boolean favorite;   // 즐겨찾기
}
