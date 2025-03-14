package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.mapper.IndexMapper;
import com.sprint.findex_team6.repository.IndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexService {
  private IndexMapper indexMapper;
  private IndexRepository indexRepository;

  public Index save(IndexInfoDto indexInfoDto){
    return indexMapper.toEntity(indexInfoDto);
  }

}
