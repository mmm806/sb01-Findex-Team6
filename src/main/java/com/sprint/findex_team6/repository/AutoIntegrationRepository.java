package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.AutoIntegration;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoIntegrationRepository extends JpaRepository<AutoIntegration, Long>, AutoIntegrationQuerydslRepository {

  @EntityGraph(attributePaths = {"index"})
  List<AutoIntegration> findAllByEnabledIsTrue();
  
  Optional<AutoIntegration> findById(@NonNull Long id);
}
