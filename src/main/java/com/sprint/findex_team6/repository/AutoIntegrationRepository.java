package com.sprint.findex_team6.repository;

import com.sprint.findex_team6.entity.AutoIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoIntegrationRepository extends JpaRepository<AutoIntegration, Long> {

}
