package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.IndexDataLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexDataLinkRepository extends JpaRepository<IndexDataLink , Long> {

}
