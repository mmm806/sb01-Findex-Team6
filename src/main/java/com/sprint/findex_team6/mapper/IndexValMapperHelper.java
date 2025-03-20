package com.sprint.findex_team6.mapper;

import com.sprint.findex_team6.entity.Index;
import java.math.BigDecimal;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class IndexValMapperHelper {

  @Named("decimalToLong")
  public Long decimalToInt(BigDecimal decimal){
    return decimal.longValue();
  }

  @Named("indexInfoIdByIndex")
  public Long indexInfoIdByIndex(Index index){
    return index.getId();
  }

}
