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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "index_data_link")
public class IndexDataLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type")
  private ConnectType sourceType;              // 유형 ("지수 정보", "지수 데이터")


  // 연동된 지수 정보
  @Column(name = "target_date")
  private LocalDateTime targetDate;     // 연동된 데이터의 날짜

  @Column(name = "worker")
  private String worker;          // 작업자 (요청 IP 또는 "system")

  @Column(name = "job_time")
  private LocalDateTime jobTime; // 작업일시

  @Column
  private Boolean result;            // 결과 ("성공", "실패")

  //지수 정보
  @ManyToOne //하나의 지수 정보는 여러번 연동 작업을 함
  @JoinColumn(name = "index_val_id")
  private IndexVal indexVal; //연동된 지수 정보
}