package com.sprint.findex_team6;

import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.mapper.IndexMapper;
import com.sprint.findex_team6.mapper.IndexMapperImpl;

public class test {

  public static void main(String[] args) {
    IndexMapper mapper = new IndexMapperImpl();

    Index idx = new Index();

    IndexInfoDto dto = mapper.toDto(idx);

    System.out.println("dto = " + dto);
  }


}
