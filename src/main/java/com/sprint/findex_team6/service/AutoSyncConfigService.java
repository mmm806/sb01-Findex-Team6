package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.AutoSyncConfigDto;
import com.sprint.findex_team6.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex_team6.repository.AutoIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@Transactional
@RequiredArgsConstructor
@RequestMapping("/api/auto-sync-configs")
public class AutoSyncConfigService {

  private final AutoIntegrationRepository autoIntegrationRepository;

  @PatchMapping("/{id}")
  private AutoSyncConfigDto modify(@RequestBody AutoSyncConfigUpdateRequest request,
      @PathVariable("id") Long id) {

    return null;
  }
}
