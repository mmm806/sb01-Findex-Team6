package com.sprint.findex_team6.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "index_id", nullable = false)
  private Index index; // 지수 정보 참조

  private Boolean favorite; // 즐겨찾기 여부

  @OneToOne
  @JoinColumn(name = "index_val_id")
  private IndexVal performanceData; // 성과 분석 정보
}
