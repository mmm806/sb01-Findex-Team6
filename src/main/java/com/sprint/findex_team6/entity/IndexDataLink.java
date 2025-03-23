package com.sprint.findex_team6.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@AllArgsConstructor
@Table(name = "index_data_link")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexDataLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", columnDefinition = "source_type")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ContentType jobType;              // 유형 ("지수 정보", "지수 데이터")


  // 연동된 지수 정보
  private LocalDate targetDate;     // 연동된 데이터의 날짜
  private String worker;          // 작업자 (요청 IP 또는 "system")
  private LocalDateTime jobTime; // 작업일시
  private Boolean result;            // 결과 ("성공", "실패")

  //지수 정보
  @ManyToOne(fetch = FetchType.LAZY) //하나의 지수 정보는 여러번 연동 작업을 함
  @JoinColumn(name = "index_id")
  private Index index; //연동된 지수 정보

  public void changeTargetDateAndIndex(LocalDate targetDate, Index index) {
    this.targetDate = targetDate;
    this.index = index;
  }
}