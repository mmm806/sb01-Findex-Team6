package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.AutoIntegration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoIntegrationRepository extends JpaRepository<AutoIntegration, Long>, AutoIntegrationQuerydslRepository {

  @EntityGraph(attributePaths = {"index"})
  List<AutoIntegration> findAllByEnabledIsTrue();

  @EntityGraph(attributePaths = {"index"})
  @NonNull
  @Override
  Optional<AutoIntegration> findById(@NonNull Long id);
}
