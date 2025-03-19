package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.Index;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexRepository extends JpaRepository<Index, Long> {
  List<Index> findByIndexClassificationContaining(String indexClassification);

  List<Index> findByIndexNameContaining(String indexName);

  List<Index> findByFavorite(boolean favorite);

  boolean existsByIndexClassificationAndIndexName(String classification, String name);

  void deleteById(Long indexInfoId);

  Optional<Index> findByIndexClassificationAndIndexName(String classification, String name);

  List<Index> findByIndexClassification(String classification);  // 임시 추가, 수정 필요

}
