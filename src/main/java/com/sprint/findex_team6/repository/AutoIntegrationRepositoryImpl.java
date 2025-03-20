package com.sprint.findex_team6.repository;

import static com.sprint.findex_team6.entity.QAutoIntegration.autoIntegration;
import static com.sprint.findex_team6.entity.QIndex.index;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.findex_team6.dto.AutoSyncConfigDto;
import com.sprint.findex_team6.dto.QAutoSyncConfigDto;
import com.sprint.findex_team6.dto.request.AutoSyncConfigCursorPageRequest;
import com.sprint.findex_team6.entity.QIndex;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class AutoIntegrationRepositoryImpl implements AutoIntegrationQuerydslRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<AutoSyncConfigDto> cursorBasePagination(AutoSyncConfigCursorPageRequest request, Pageable slice) {

    //boolean enabled = request.enabled() != null;

    List<AutoSyncConfigDto> paged = queryFactory
        .select(new QAutoSyncConfigDto(
            autoIntegration.id,
            index.id,
            index.indexClassification,
            index.indexName,
            autoIntegration.enabled
        ))
        .from(autoIntegration)
        .leftJoin(autoIntegration.index, index)
        .where(
            indexInfoIdEq(request),
            enabledEq(request.enabled()),
            cursor(request)
        )
        .orderBy(sortFieldAndDirection(request.sortField(), request.sortDirection()))
        .limit(getSize(request.size()) + 1) // 커서 페이징을 위해서 1번 더 쿼리
        .fetch();

    boolean hasNext = paged.size() > getSize(request.size());

    return new SliceImpl<>(paged, slice, hasNext);
  }

  /**
  * @methodName : cursor
  * @date : 2025-03-20 오전 10:09
  * @author : wongil
  * @Description: 커서페이징
  **/
  private BooleanExpression cursor(AutoSyncConfigCursorPageRequest request) {
    if (request.cursor() == null) {
      return null;
    }

    String sortField = request.sortField() == null ? String.valueOf(index.indexName) : request.sortField();
    boolean isDesc =
        request.sortDirection() == null || request.sortDirection().isBlank()
            || "desc".equalsIgnoreCase(request.sortDirection());

    //String s = new String(Base64.getDecoder().decode(request.cursor()));

    Long idAfter = request.idAfter();
    if (idAfter == null) {
      return null;
    }

    if(sortField.equals("enable")) { // 활성화 여부로 정렬
      // 뭐로 커서 페이징해야하는지 모르겠어서 일단 활성화 여부를 커서로 지정
      Boolean enabledBaseCursor = Boolean.valueOf(request.cursor());

      return getEnableBaseCondition(enabledBaseCursor, idAfter, isDesc);
    }
    else { // indexInfoName으로 정렬시
      String indexInfoNameBaseCursor = request.cursor();

      return getIndexInfoNameBaseCondition(indexInfoNameBaseCursor, idAfter, isDesc);
    }
  }

  /**
  * @methodName : getIndexInfoNameBaseCondition
  * @date : 2025-03-20 오전 11:19
  * @author : wongil
  * @Description: 정렬 필드가 indexInfo.indexName으로 들어오면 어떻게 페이징할 것 인지
  **/
  private BooleanExpression getIndexInfoNameBaseCondition(String indexInfoNameBaseCursor, Long idAfter, boolean isDesc) {
    BooleanExpression nextId = null;
    BooleanExpression baseCondition = null;

    if (isDesc) {
      baseCondition = index.indexName.loe(indexInfoNameBaseCursor);
    }
    else {
      baseCondition = index.indexName.goe(indexInfoNameBaseCursor);
    }

    if (idAfter != null) {
      if(isDesc){
        nextId = autoIntegration.id.lt(idAfter);
      }
      else {
        nextId = autoIntegration.id.gt(idAfter);
      }
    }

    return baseCondition.and(nextId);
  }

  /**
  * @methodName : getEnableBaseCondition
  * @date : 2025-03-20 오전 11:20
  * @author : wongil
  * @Description: 정렬 필드가 enabled로 들어오면 어떻게 페이징할 것 인지
  **/
  private BooleanExpression getEnableBaseCondition(Boolean enabledBaseCursor, Long idAfter, boolean isDesc) {
    BooleanExpression nextId = null;
    BooleanExpression baseCondition = null;

    if (isDesc) {
      baseCondition = autoIntegration.enabled.loe(enabledBaseCursor); // true면 true보다 작은 false가 내려옴
    }
    else {
      baseCondition = autoIntegration.enabled.goe(enabledBaseCursor); // true면 true보다 큰 true만
    }

    if (idAfter != null) {
      if(isDesc){
        nextId = autoIntegration.id.lt(idAfter);
      }
      else {
        nextId = autoIntegration.id.gt(idAfter);
      }
    }

    baseCondition.and(nextId);

    return baseCondition;
  }


  /**
  * @methodName : sortFieldAndDirection
  * @date : 2025-03-20 오전 10:05
  * @author : wongil
  * @Description:
   * default sort field => indexInfo.IndexName
   * default sort direction => desc
  **/
  private OrderSpecifier<?> sortFieldAndDirection(String field, String direction) {
    String sortField =  (field == null) ? String.valueOf(QIndex.index.indexName) : field;
    boolean isDesc = direction == null || direction.isBlank() || "desc".equalsIgnoreCase(direction);

    if (sortField.equalsIgnoreCase("enable")) {
      return isDesc
          ? autoIntegration.enabled.desc()
          : autoIntegration.enabled.asc();
    }
    else {
      return isDesc
          ? index.indexName.desc()
          : index.indexName.asc();
    }
  }

  private Long getSize(Integer size) {
    return size != null
        ? Long.valueOf(size)
        : 10;
  }

  /**
  * @methodName : enabledEq
  * @date : 2025-03-20 오전 9:49
  * @author : wongil
  * @Description: request enabled에 따라 원하는 데이터 뽑기
  **/
  private BooleanExpression enabledEq(Boolean enabled) {

    return enabled != null ? autoIntegration.enabled.eq(enabled) : null;
  }

  /**
  * @methodName : indexInfoIdEq
  * @date : 2025-03-20 오전 9:49
  * @author : wongil
  * @Description: indexInfoId랑 같은 autoIntegeration 뽑기
  **/
  private BooleanExpression indexInfoIdEq(AutoSyncConfigCursorPageRequest request) {

    return request.indexInfoId() != null
        ? autoIntegration.index.id.eq(request.indexInfoId())
        : null;
  }

}
