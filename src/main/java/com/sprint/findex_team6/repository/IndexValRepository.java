package com.sprint.findex_team6.repository;


import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexVal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexValRepository extends JpaRepository<IndexVal, Long> {

  List<IndexVal> findByIndex_Id(Long id);

  boolean existsByIndex_IdIn(List<Long> ids);

  @EntityGraph(attributePaths = {"index"})
  List<IndexVal> findByIndex_IdIn(List<Integer> ids);

  //Dashboard
  List<IndexVal> findByIndexIdInAndBaseDateIn(List<Long> indexIds, List<LocalDate> startDate);

  Optional<IndexVal> findFirstByIndexAndBaseDateGreaterThanOrderByBaseDateAsc(Index index, LocalDate baseDate);

  Optional<IndexVal> findFirstByIndexAndBaseDateLessThanOrderByBaseDateDesc(Index index, LocalDate baseDate);

  List<IndexVal> findByIndexAndBaseDateBetweenOrderByBaseDateAsc(Index index, LocalDate startDate, LocalDate endDate);

  List<IndexVal> findByIndexIdAndBaseDateBetween(Long indexId, LocalDate startDate, LocalDate endDate, Sort sort);

  Optional<IndexVal> findByIndex_IdAndBaseDate(Long indexId, LocalDate baseDate);

  Page<IndexVal> findByIndex_IdAndBaseDateBetween(
      Long indexId, LocalDate startDate, LocalDate endDate,
      Pageable pageable);

  @Query("SELECT iv FROM IndexVal iv "
      + "WHERE iv.index.id = :indexId "
      + "AND iv.baseDate BETWEEN :startDate AND :endDate "
      + "AND (iv.baseDate < :cursor OR (iv.baseDate = :cursor AND iv.id < :idAfter)) ")
  Page<IndexVal> findByBaseDateCursorDesc(
      Long indexId, LocalDate startDate, LocalDate endDate,
      LocalDate cursor, Long idAfter, Pageable pageable);

  @Query("SELECT iv FROM IndexVal iv "
      + "WHERE iv.index.id = :indexId "
      + "AND iv.baseDate BETWEEN :startDate AND :endDate "
      + "AND (iv.baseDate > :cursor OR (iv.baseDate = :cursor AND iv.id > :idAfter)) ")
  Page<IndexVal> findByBaseDateCursorAsc(
      Long indexId, LocalDate startDate, LocalDate endDate,
      LocalDate cursor, Long idAfter, Pageable pageable);

  @Query("SELECT iv FROM IndexVal iv "
      + "WHERE iv.index.id = :indexId "
      + "AND iv.baseDate BETWEEN :startDate AND :endDate "
      + "AND (iv.closingPrice < :cursor OR (iv.closingPrice = :cursor AND iv.id < :idAfter)) ")
  Page<IndexVal> findByClosingPriceCursorDesc(
      Long indexId, LocalDate startDate, LocalDate endDate,
      BigDecimal cursor, Long idAfter, Pageable pageable);

  @Query("SELECT iv FROM IndexVal iv "
      + "WHERE iv.index.id = :indexId "
      + "AND iv.baseDate BETWEEN :startDate AND :endDate "
      + "AND (iv.closingPrice > :cursor OR (iv.closingPrice = :cursor AND iv.id > :idAfter)) ")
  Page<IndexVal> findByClosingPriceCursorAsc(
      Long indexId, LocalDate startDate, LocalDate endDate,
      BigDecimal cursor, Long idAfter, Pageable pageable);

}
