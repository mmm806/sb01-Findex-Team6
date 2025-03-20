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
<
  List<Index> findByIndexClassificationContaining(String indexClassification);

  List<Index> findByIndexNameContaining(String indexName);

  List<Index> findByFavorite(boolean favorite);

  boolean existsByIndexClassificationAndIndexName(String classification, String indexName);

  void deleteById(Long indexId);

  Optional<Index> findByIndexClassificationAndIndexName(String classification, String indexName);

  List<Index> findByIndexClassification(String classification); 

  Optional<Index> findByIndexName(String indexName);

  List<Index> findAllByIdIn(Collection<Integer> ids);

  Optional<Index> findById(Long id);

  Page<Index> findAll(Pageable pageable);

  List<Index> findByIdGreaterThan(Long idAfter, Pageable pageable);

  List<IndexInfoSummaryDto> findAllProjectBy();

  List<Index> findByIndexClassificationContainingAndIndexNameContainingAndFavorite(
          String indexClassification, String indexName, Boolean favorite, Pageable pageable);

  List<Index> findByIdGreaterThanAndIndexClassificationContainingAndIndexNameContainingAndFavorite(
          Long idAfter, String indexClassification, String indexName, Boolean favorite, Pageable pageable);
}
