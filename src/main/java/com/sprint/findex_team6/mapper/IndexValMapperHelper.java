package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.entity.Index;
import java.math.BigDecimal;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class IndexValMapperHelper {

  @Named("decimalToInt")
  public Integer decimalToInt(BigDecimal decimal){
    return decimal.intValue();
  }

  @Named("longToInt")
  public Integer longToInt(Long num){
    return num.intValue();
  }

  @Named("indexInfoIdByIndex")
  public Long indexInfoIdByIndex(Index index){
    return index.getId();
  }

}
