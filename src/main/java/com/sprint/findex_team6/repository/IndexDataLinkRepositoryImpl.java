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
import com.sprint.findex_team6.dto.request.SyncCursorPageRequest;
import com.sprint.findex_team6.entity.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IndexDataLinkRepositoryImpl implements IndexDataLinkQuerydslRepository {

  private final JPAQueryFactory queryFactory;

  /**
   * @methodName : search
   * @date : 2025-03-17 오후 1:58
   * @author : wongil
   * @Description: querydsl로 구현 CursorPageResponseSyncJobDto
   **/
  @Override
  public Slice<SyncJobDto> cursorBasePagination(SyncCursorPageRequest request, Pageable slice) {

    List<SyncJobDto> paged = queryFactory
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
            jobTypeEq(request.getJobType()),
            cursor(request)
        )
        .orderBy(sortFieldAndDirection(request.getSortField(), request.getSortDirection()))
        .orderBy(indexDataLink.id.desc())
        .limit(getSize(request) + 1) // nextCursor, nextIdAfter 구하기 위해서 1개 더 쿼리
        .fetch();

    boolean hasNext = paged.size() > getSize(request);

    return new SliceImpl<SyncJobDto>(paged, slice, hasNext);
  }

  /**
   * @methodName : cursorBasePaginationTotalCount
   * @date : 2025-03-19 오후 5:41
   * @author : wongil
   * @Description: 카운트 쿼리
   **/
  @Override
  public Long cursorBasePaginationTotalCount(SyncCursorPageRequest request) {
    return queryFactory
        .select(indexDataLink.count())
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
            jobTypeEq(request.getJobType()),
            cursor(request)
        )
        .fetchOne();
  }

  /**
   * @methodName : cursor
   * @date : 2025-03-19 오후 3:11
   * @author : wongil
   * @Description: 커서 기반 페이징
   * ?sortField=jobTime&sortDirection=desc&cursor=2025-03-19T04:03:46.008145&idAfter=9934 cursor =
   * 다음 페이지 시작점 idAfter = 이전 페이지의 마지막 요소
   **/
  private BooleanExpression cursor(SyncCursorPageRequest request) {
    if (request.getCursor() == null) {
      return null;
    }

    String field = request.getSortField();
    String sortField = (field == null || field.isBlank()) ? "jobTime" : request.getSortField();

    String direction = request.getSortDirection();
    boolean isDesc =
        direction == null || direction.isBlank() || direction.equalsIgnoreCase("desc");

    LocalDateTime cursor = LocalDateTime.parse(request.getCursor());
    LocalDate localDateCursor = LocalDateTime.parse(request.getCursor()).toLocalDate();
    Long idAfter = request.getIdAfter();

    // 정렬 필드가 jobTime이고 내림차순 정렬일 때
    if (sortField.equals("jobTime")) {
      if (isDesc) {

        BooleanExpression lowerThanEqualCursor = getJobTimeDescBaseCondition(cursor, idAfter);

        if (request.getJobTimeFrom() != null) {
          return lowerThanEqualCursor
              .or(indexDataLink.jobTime.eq(request.getJobTimeFrom()) // 작업 시간이 같으면
                  .and(indexDataLink.id.lt(idAfter)) // idAfter보다 작은 id 값
              );
        }

        return lowerThanEqualCursor;
      } else { // 오름차순

        BooleanExpression greaterThanEqualCursor = getJobTimeAscBaseCondition(cursor, idAfter);

        if (request.getJobTimeFrom() != null) {
          return greaterThanEqualCursor
              .or(indexDataLink.jobTime.eq(request.getJobTimeFrom()) // 작업 시간이 같으면
                  .and(indexDataLink.id.gt(idAfter)) // idAfter보다 큰 id 값
              );
        }

        return greaterThanEqualCursor;
      }
    } else { // "대상 날짜" baseTime을 내림차순 쿼리
      if (isDesc) {

        BooleanExpression lowerThanEqualBaseDateCursor = getDateDescBaseCondition(localDateCursor, idAfter);

        if (localDateCursor != null) {
          return lowerThanEqualBaseDateCursor
              .or(indexDataLink.targetDate.eq(localDateCursor)
                  .and(indexDataLink.id.lt(idAfter))
              );
        }

        return lowerThanEqualBaseDateCursor;
      } else { // 오름차순 정렬

        BooleanExpression lowerThanEqualBaseDateCursor = getDateAscBaseCondition(localDateCursor, idAfter);

        if (localDateCursor != null) {
          return lowerThanEqualBaseDateCursor
              .or(indexDataLink.targetDate.eq(localDateCursor)
                  .and(indexDataLink.id.gt(idAfter))
              );
        }

        return lowerThanEqualBaseDateCursor;
      }
    }

  }

  /**
  * @methodName : getDateAscBaseCondition
  * @date : 2025-03-19 오후 6:53
  * @author : wongil
  * @Description: TargetDate 기준 오름차순
  **/
  private BooleanExpression getDateAscBaseCondition(LocalDate localDateCursor, Long idAfter) {
    BooleanExpression lowerThanEqualBaseDateCursor = indexDataLink.targetDate.loe(localDateCursor);
    BooleanExpression nextId = null;

    if (idAfter != null) {
      nextId = indexDataLink.id.lt(idAfter);
    }

    lowerThanEqualBaseDateCursor.and(nextId);
    return lowerThanEqualBaseDateCursor;
  }

  /**
  * @methodName : getDateDescBaseCondition
  * @date : 2025-03-19 오후 6:51
  * @author : wongil
  * @Description: TargetDate를 기준으로 내림차순 정렬
  **/
  private BooleanExpression getDateDescBaseCondition(LocalDate localDateCursor, Long idAfter) {
    BooleanExpression lowerThanEqualBaseDateCursor = indexDataLink.targetDate.loe(localDateCursor);
    BooleanExpression nextId = null;

    if (idAfter != null) {
      nextId = indexDataLink.id.lt(idAfter);
    }

    return lowerThanEqualBaseDateCursor.and(nextId);
  }

  /**
  * @methodName : getAscBaseCondition
  * @date : 2025-03-19 오후 6:43
  * @author : wongil
  * @Description: (오름차순) 커서 시간보다 크고 idAfter보다 큰 것을 뽑을 것임
  **/
  private BooleanExpression getJobTimeAscBaseCondition(LocalDateTime cursor, Long idAfter) {
    BooleanExpression greaterThanEqualCursor = indexDataLink.jobTime.goe(cursor);
    BooleanExpression nextId = null;

    if (idAfter != null) {
      nextId = indexDataLink.id.gt(idAfter);
    }

    return greaterThanEqualCursor.and(nextId);
  }

  /**
  * @methodName : getBaseCondition
  * @date : 2025-03-19 오후 6:38
  * @author : wongil
  * @Description: (내림차순) 커서 시간보다 작고, idAfter보다 작은 데이터만 뽑을 것임
  **/
  private BooleanExpression getJobTimeDescBaseCondition(LocalDateTime cursor, Long idAfter) {
    BooleanExpression lowerThanEqualCursor = indexDataLink.jobTime.loe(cursor);
    BooleanExpression nextId = null;

    if (idAfter != null) {
      nextId = indexDataLink.id.lt(idAfter);
    }

    return lowerThanEqualCursor.and(nextId);
  }

  /**
   * @methodName : jobTypeEq
   * @date : 2025-03-19 오후 2:49
   * @author : wongil
   * @Description: 지수 정보면 지수 정보로 검색 아니면 지수 데이터 조건으로 검색
   **/
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

    boolean isDesc =
        sortDirection == null || sortDirection.isBlank() || "desc".equalsIgnoreCase(sortDirection);

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
  private int getSize(SyncCursorPageRequest request) {
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
  private BooleanExpression jobTimeTo(SyncCursorPageRequest request) {
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
  private BooleanExpression jobTimeFrom(SyncCursorPageRequest request) {
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
  private BooleanExpression workerEq(SyncCursorPageRequest request) {
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
  private BooleanExpression baseDateTo(SyncCursorPageRequest request) {
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
  private BooleanExpression baseDateFrom(SyncCursorPageRequest request) {
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
  private BooleanExpression indexInfoIdEq(SyncCursorPageRequest request) {
    return request.getIndexInfoId() != null
        ? index.id.eq(request.getIndexInfoId())
        : null;
  }
}
