package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.dto.IndexInfoSummaryDto;
import com.sprint.findex_team6.entity.Index;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query("""
  SELECT i FROM Index i
  WHERE (:indexClassification IS NULL OR i.indexClassification = :indexClassification)
  AND (:indexName IS NULL OR i.indexName LIKE CONCAT('%', :indexName, '%'))
  AND (:favorite IS NULL OR i.favorite = :favorite)
  AND (i.indexClassification > :cursor OR (i.indexClassification = :cursor AND i.id > :idAfter))
  """)
    Page<Index> findByIndexClassificationCursorAsc(
            String indexClassification,
            String indexName,
            Boolean favorite,
            String cursor,
            Long idAfter,
            Pageable pageable
    );


    @Query("""
  SELECT i FROM Index i
  WHERE (:indexClassification IS NULL OR i.indexClassification = :indexClassification)
  AND (:indexName IS NULL OR i.indexName LIKE CONCAT('%', :indexName, '%'))
  AND (:favorite IS NULL OR i.favorite = :favorite)
  AND (i.indexClassification < :cursor OR (i.indexClassification = :cursor AND i.id < :idAfter))
  """)
    Page<Index> findByIndexClassificationCursorDesc(
            String indexClassification,
            String indexName,
            Boolean favorite,
            String cursor,
            Long idAfter,
            Pageable pageable
    );


    @Query("""
  SELECT i FROM Index i
  WHERE (:indexClassification IS NULL OR i.indexClassification = :indexClassification)
  AND (:indexName IS NULL OR i.indexName LIKE CONCAT('%', :indexName, '%'))
  AND (:favorite IS NULL OR i.favorite = :favorite)
  AND (i.indexName > :cursor OR (i.indexName = :cursor AND i.id > :idAfter))
  """)
    Page<Index> findByIndexNameCursorAsc(
            String indexClassification,
            String indexName,
            Boolean favorite,
            String cursor,
            Long idAfter,
            Pageable pageable
    );


    @Query("""
  SELECT i FROM Index i
  WHERE (:indexClassification IS NULL OR i.indexClassification = :indexClassification)
  AND (:indexName IS NULL OR i.indexName LIKE CONCAT('%', :indexName, '%'))
  AND (:favorite IS NULL OR i.favorite = :favorite)
  AND (i.indexName < :cursor OR (i.indexName = :cursor AND i.id < :idAfter))
  """)
    Page<Index> findByIndexNameCursorDesc(
            String indexClassification,
            String indexName,
            Boolean favorite,
            String cursor,
            Long idAfter,
            Pageable pageable
    );


    List<Index> findAll();


    @Query("""
  SELECT i FROM Index i
  WHERE (:indexClassification IS NULL OR i.indexClassification = :indexClassification)
  AND (:indexName IS NULL OR i.indexName LIKE CONCAT('%', :indexName, '%'))
  AND (:favorite IS NULL OR i.favorite = :favorite)
  ORDER BY i.id ASC
  """)
    Page<Index> findAllByConditions(
            String indexClassification,
            String indexName,
            Boolean favorite,
            Pageable pageable
    );

}
