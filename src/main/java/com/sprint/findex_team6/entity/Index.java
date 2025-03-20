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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private SourceType sourceType;    // 소스 타입

  @Column
  private Boolean favorite;   // 즐겨찾기

  public Index(String indexClassification, String indexName, Integer employedItemsCount,
      LocalDate baseDate, BigDecimal baseIndex, SourceType sourceType, Boolean favorite) {
    this.indexClassification = indexClassification;
    this.indexName = indexName;
    this.employedItemsCount = employedItemsCount;
    this.baseDate = baseDate;
    this.baseIndex = baseIndex;
    this.sourceType = sourceType;
    this.favorite = favorite;
  }

  public void updateInfo(String indexClassification, String idxNm, int employedItemsCount,
      LocalDate baseDate, BigDecimal baseIndex) {

    this.indexClassification = indexClassification;
    this.indexName = idxNm;
    this.employedItemsCount = employedItemsCount;
    this.baseDate = baseDate;
    this.baseIndex = baseIndex;
  }
}
