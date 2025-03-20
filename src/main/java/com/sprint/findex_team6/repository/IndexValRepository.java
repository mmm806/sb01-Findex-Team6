package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.IndexVal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexValRepository extends JpaRepository<IndexVal, Long> {

  Optional<IndexVal> findAllByIndex_IdAndDate(Long indexId, LocalDate date);
}
