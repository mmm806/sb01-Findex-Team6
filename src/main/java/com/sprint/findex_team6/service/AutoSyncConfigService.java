package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.AutoSyncConfigDto;
import com.sprint.findex_team6.dto.request.AutoSyncConfigCursorPageRequest;
import com.sprint.findex_team6.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex_team6.dto.response.CursorPageResponseSyncDto;
import com.sprint.findex_team6.entity.AutoIntegration;
import com.sprint.findex_team6.repository.AutoIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AutoSyncConfigService {

  private final AutoIntegrationRepository autoIntegrationRepository;

  public AutoSyncConfigDto modify(AutoSyncConfigUpdateRequest request, Long id) {

    AutoIntegration autoIntegration = autoIntegrationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("찾는 자동 연동 설정이 없습니다."));

    autoIntegration.changeEnable(request.enable());

    return AutoSyncConfigDto.builder()
        .id(autoIntegration.getId())
        .indexInfoId(autoIntegration.getIndex().getId())
        .indexClassification(autoIntegration.getIndex().getIndexClassification())
        .indexName(autoIntegration.getIndex().getIndexName())
        .build();
  }


  public CursorPageResponseSyncDto<AutoSyncConfigDto> search(
      AutoSyncConfigCursorPageRequest request) {

    AutoSyncConfigDto paged = autoIntegrationRepository.cursorBasePagination(request)
        .orElseThrow(() -> new RuntimeException("페이징에 실패했습니다."));

    return null;
  }
}
