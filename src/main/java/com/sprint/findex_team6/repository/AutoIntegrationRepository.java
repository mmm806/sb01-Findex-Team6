package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.AutoIntegration;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoIntegrationRepository extends JpaRepository<AutoIntegration, Long> {

  @EntityGraph(attributePaths = {"index"})
  List<AutoIntegration> findAllByEnabledIsTrue();
}
