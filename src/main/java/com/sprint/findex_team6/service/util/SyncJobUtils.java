package com.sprint.findex_team6.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.entity.ContentType;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyncJobUtils {

  /**
  * @methodName : findItems
  * @date : 2025-03-18 오후 2:14
  * @author : wongil
  * @Description: API 응답 결과 파싱해서 JsonNode 타입으로 반환
  **/
  public static JsonNode findItems(String response) {
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      return objectMapper.readTree(response);
    } catch (JsonProcessingException e) {
      log.error("Raw API response: {}", response);
      throw new RuntimeException(e);
    }
  }

  /**
  * @methodName : getNumOfRows
  * @date : 2025-03-18 오후 2:14
  * @author : wongil
  * @Description: API 응답 바디에서 numOfRows 뽑기
  **/
  public static int getNumOfRows(JsonNode items) {
    return items.path("response")
        .path("body")
        .path("numOfRows")
        .asInt();
  }

  /**
  * @methodName : getTotalPages
  * @date : 2025-03-18 오후 2:15
  * @author : wongil
  * @Description: 총 페이지 수 구하기
  **/
  public static int getTotalPages(JsonNode items) {
    int numOfRows = getNumOfRows(items);
    int totalCount = getTotalCount(items);

    return (int) Math.ceil((double) totalCount / numOfRows);
  }

  /**
  * @methodName : getTotalCount
  * @date : 2025-03-18 오후 2:15
  * @author : leeco
  * @Description: 데이터의 총 개수 가져오기
  **/
  public static int getTotalCount(JsonNode items) {
    return items.path("response")
        .path("body")
        .path("totalCount")
        .asInt();
  }

  /**
  * @methodName : createMockSyncJobResponse
  * @date : 2025-03-18 오후 2:26
  * @author : wongil
  * @Description: 가짜 응답용 객체 생성
  **/
  public static List<SyncJobDto> createMockSyncJobResponse(JsonNode items, HttpServletRequest request,
      List<SyncJobDto> syncIndexInfoJobDtoList, ContentType jobType) {

    int totalCount = getTotalCount(items);

    for (long indexInfoId = 1L; indexInfoId <= totalCount; indexInfoId++) {
      SyncJobDto dto = SyncJobDto.builder()
          .id(null)
          .jobType(jobType)
          .indexInfoId(indexInfoId)
          .targetDate(null)
          .worker(getUserIp(request))
          .jobTime(LocalDateTime.now())
          .result("SUCCESS")
          .build();

      syncIndexInfoJobDtoList.add(dto);
    }

    return syncIndexInfoJobDtoList;
  }

  /**
  * @methodName : getUserIp
  * @date : 2025-03-18 오후 2:26
  * @author : wongil
  * @Description: 클라이언트 IP 얻기
  **/
  public static String getUserIp(HttpServletRequest request) {
    return request.getRemoteAddr();
  }
}
