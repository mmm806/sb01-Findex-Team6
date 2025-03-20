package com.sprint.findex_team6.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auto_integration")
public class AutoIntegration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_id", nullable = false)
  private Index index; // 지수 정보 참조

  @Column
  @Setter
  private Boolean enabled; // 활성화 여부

  public void changeEnable(boolean enabled) {
    this.enabled = enabled;
  }

  public AutoIntegration(Index index, Boolean enabled) {
    this.index = index;
    this.enabled = enabled;
  }
}
