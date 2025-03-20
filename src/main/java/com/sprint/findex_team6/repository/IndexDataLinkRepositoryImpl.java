package com.sprint.findex_team6.repository;

import static com.sprint.findex_team6.entity.QIndex.index;
import static com.sprint.findex_team6.entity.QIndexDataLink.indexDataLink;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.findex_team6.dto.QSyncJobDto;
import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.CursorPageRequest;
import com.sprint.findex_team6.entity.ContentType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IndexDataLinkRepositoryImpl implements IndexDataLinkRepositoryQuerydsl {

  private final JPAQueryFactory queryFactory;

  /**
   * @methodName : search
   * @date : 2025-03-17 오후 1:58
   * @author : wongil
   * @Description: querydsl로 구현 CursorPageResponseSyncJobDto
   **/
  @Override
  public List<SyncJobDto> search(CursorPageRequest request) {

    return queryFactory
        .select(new QSyncJobDto(
            indexDataLink.id,
            indexDataLink.jobType,
            index.id,
            indexDataLink.targetDate,
            indexDataLink.worker,
            indexDataLink.jobTime,
            isResult(indexDataLink.result)
        ))
        .from(indexDataLink)
        .leftJoin(indexDataLink.index, index)
        .where(
            indexInfoIdEq(request),
            baseDateFrom(request),
            baseDateTo(request),
            workerEq(request),
            jobTimeFrom(request),
            jobTimeTo(request),
            statusEq(request.getStatus()),
            jobTypeEq(request.getJobType())
        )
        .orderBy(sortFieldAndDirection(request.getSortField(), request.getSortDirection()))
        .limit(getSize(request))
        .fetch();
  }

  private BooleanExpression jobTypeEq(ContentType jobType) {
    if (jobType != null) {
      return jobType.equals(ContentType.INDEX_INFO)
          ? indexDataLink.jobType.eq(ContentType.INDEX_INFO)
          : indexDataLink.jobType.eq(ContentType.INDEX_DATA);
    }

    return null;
  }

  /**
  * @methodName : sortFieldAndDirection
  * @date : 2025-03-17 오후 4:25
  * @author : wongil
  * @Description: 정렬 필드(기본값 jobTime)을 기준으로 정렬(기본값 desc)
  **/
  private OrderSpecifier<?> sortFieldAndDirection(String sortField, String sortDirection) {

    String field = (sortField == null || sortField.isBlank()) ? "jobTime" : sortField;

    boolean isDesc = sortDirection == null || sortDirection.isBlank() || "desc".equalsIgnoreCase(sortDirection);


    if (field.equals("targetDate")) {
      return isDesc
          ? indexDataLink.targetDate.desc()
          : indexDataLink.targetDate.asc();
    } else {
      return isDesc
          ? indexDataLink.jobTime.desc()
          : indexDataLink.jobTime.asc();
    }
  }

  /**
  * @methodName : isResult
  * @date : 2025-03-17 오후 4:26
  * @author : wongil
  * @Description: db에서 꺼낸 값이 true면 SUCCESS 아니면 FAILED로 변환
  **/
  private Expression<String> isResult(BooleanPath result) {
    return new CaseBuilder()
        .when(result.isTrue()).then("SUCCESS")
        .otherwise("FAILED");
  }

  /**
   * @methodName : getSize
   * @date : 2025-03-17 오후 3:53
   * @author : wongil
   * @Description: 한번에 몇개씩 가져올 건지 구함. null이면 기본값으로 10개씩
   **/
  private int getSize(CursorPageRequest request) {
    return request.getSize() != null
        ? request.getSize()
        : 10;
  }

  /**
  * @methodName : statusEq
  * @date : 2025-03-17 오후 4:26
  * @author : wongil
  * @Description: 쿼리 조건 중 작업상태(status)가 success면 db에서 꺼낸 indexDataLink의 result가 true인 것만 뽑음
  **/
  private BooleanExpression statusEq(String status) {
    if (status != null) {
      if (status.equals("SUCCESS")) {
        return indexDataLink.result.eq(true);
      } else if (status.equals("FAILED")) {
        return indexDataLink.result.eq(false);
      }
    }

    return null;
  }

  /**
  * @methodName : jobTimeTo
  * @date : 2025-03-17 오후 4:28
  * @author : wongil
  * @Description: jobTime보다 작은 시간을 가진거 뽑기
  **/
  private BooleanExpression jobTimeTo(CursorPageRequest request) {
    return request.getJobTimeTo() != null
        ? indexDataLink.jobTime.loe(request.getJobTimeTo())
        : null;
  }

  /**
  * @methodName : jobTimeFrom
  * @date : 2025-03-17 오후 4:29
  * @author : wongil
  * @Description: jobTime보다 큰 시간
  **/
  private BooleanExpression jobTimeFrom(CursorPageRequest request) {
    return request.getJobTimeFrom() != null
        ? indexDataLink.jobTime.goe(request.getJobTimeFrom())
        : null;
  }

  /**
   * @methodName : workerEq
   * @date : 2025-03-17 오후 3:38
   * @author : wongil
   * @Description: 파라미터로 넘어오면 worker랑 같은 애들 뽑기
   **/
  private BooleanExpression workerEq(CursorPageRequest request) {
    return request.getWorker() != null
        ? indexDataLink.worker.eq(request.getWorker())
        : null;
  }

  /**
   * @methodName : baseDateTo
   * @date : 2025-03-17 오후 3:36
   * @author : wongil
   * @Description: index의 기준 시점을 기준으로 같거나 그 이전 날부터 돌아가며 데이터 뽑기
   **/
  private BooleanExpression baseDateTo(CursorPageRequest request) {
    return request.getBaseDateTo() != null
        ? index.baseDate.loe(request.getBaseDateTo())
        : null;
  }

  /**
   * @methodName : baseDateFrom
   * @date : 2025-03-17 오후 3:33
   * @author : wongil
   * @Description: index의 기준 시점을 기준으로 같거나 다음날부터 데이터 뽑기
   **/
  private BooleanExpression baseDateFrom(CursorPageRequest request) {
    System.out.println("baseDateFrom: " + request.getBaseDateFrom());
    return request.getBaseDateFrom() != null
        ? index.baseDate.goe(request.getBaseDateFrom())
        : null;
  }

  /**
   * @methodName : indexInfoIdEq
   * @date : 2025-03-17 오후 3:22
   * @author : wongil
   * @Description: index의 id와 파라미터로 넘어온 IndexInfoId가 같은지 비교
   **/
  private BooleanExpression indexInfoIdEq(CursorPageRequest request) {
    return request.getIndexInfoId() != null
        ? index.id.eq(request.getIndexInfoId())
        : null;
  }
}
