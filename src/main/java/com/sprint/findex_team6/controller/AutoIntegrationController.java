package com.sprint.findex_team6.controller;

import com.sprint.findex_team6.dto.AutoSyncConfigDto;
import com.sprint.findex_team6.dto.request.AutoSyncConfigCursorPageRequest;
import com.sprint.findex_team6.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex_team6.dto.response.CursorPageResponseSyncDto;
import com.sprint.findex_team6.service.AutoSyncConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auto-sync-configs")
public class AutoIntegrationController {

  private final AutoSyncConfigService autoSyncConfigService;

  @PatchMapping("/{id}")
  private AutoSyncConfigDto modify(@RequestBody AutoSyncConfigUpdateRequest request,
      @PathVariable("id") Long id) {

    return autoSyncConfigService.modify(request, id);
  }

  @GetMapping
  private CursorPageResponseSyncDto<AutoSyncConfigDto> findAutoSyncConfigs(@ModelAttribute AutoSyncConfigCursorPageRequest request, Pageable pageable) {

    return autoSyncConfigService.search(request, pageable);
  }
}
