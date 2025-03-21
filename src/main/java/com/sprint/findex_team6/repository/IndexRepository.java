package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.dto.IndexInfoSummaryDto;
import com.sprint.findex_team6.entity.Index;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexRepository extends JpaRepository<Index, Long> {
  
  Optional<Index> findByIndexName(String indexName);
  
  List<Index> findByIndexClassificationContaining(String indexClassification);

  List<Index> findByIndexNameContaining(String indexName);

  List<Index> findByFavorite(boolean favorite);

  boolean existsByIndexClassificationAndIndexName(String classification, String indexName);

  void deleteById(Long indexId);

  Optional<Index> findByIndexClassificationAndIndexName(String classification, String indexName);

  List<Index> findByIndexClassification(String classification); 

  List<Index> findAllByIdIn(Collection<Integer> ids);

  Optional<Index> findById(Long id);

  Page<Index> findAll(Pageable pageable);

  List<Index> findByIdGreaterThan(Long idAfter, Pageable pageable);

  List<Index> findByIndexClassificationAndIndexNameAndFavorite(
          String indexClassification, String indexName, Boolean favorite, Pageable pageable);
  List<Index> findByIndexClassificationCursorAsc(
          String cursor, Pageable pageable);

  List<Index> findByIndexClassificationCursorDesc(
          String cursor, Pageable pageable);

  List<Index> findByIndexNameCursorAsc(
          String cursor, Pageable pageable);

  List<Index> findByIndexNameCursorDesc(
          String cursor, Pageable pageable);

  List<Index> findAll();
}
