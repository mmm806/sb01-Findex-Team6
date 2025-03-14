package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.Index;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexRepository extends JpaRepository<Index, Long> {
  Optional<Index> findByIndexName(String indexName);
}
