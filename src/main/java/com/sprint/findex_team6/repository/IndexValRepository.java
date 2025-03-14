package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.IndexVal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexValRepository extends JpaRepository<IndexVal, Long> {

}
