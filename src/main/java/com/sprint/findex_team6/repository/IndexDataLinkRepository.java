package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.IndexDataLink;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataLinkRepository extends JpaRepository<IndexDataLink, Long>,
    IndexDataLinkRepositoryQuerydsl {

  @EntityGraph(attributePaths = {"index"})
  List<IndexDataLink> findByIndex_Id(Long indexId);
}
