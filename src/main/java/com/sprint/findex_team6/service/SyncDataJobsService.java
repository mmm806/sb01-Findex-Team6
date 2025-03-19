package com.sprint.findex_team6.service;

import static com.sprint.findex_team6.service.util.SyncJobUtils.getUserIp;

import com.fasterxml.jackson.databind.JsonNode;
import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.IndexDataSyncRequest;
import com.sprint.findex_team6.entity.ContentType;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexDataLink;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.repository.IndexDataLinkRepository;
import com.sprint.findex_team6.repository.IndexRepository;
import com.sprint.findex_team6.repository.IndexValRepository;
import com.sprint.findex_team6.service.util.SyncJobUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SyncDataJobsService {

  private final IndexDataLinkRepository indexDataLinkRepository;
  private final IndexRepository indexRepository;
  private final IndexValRepository indexValRepository;

  private final PlatformTransactionManager transactionManager;
  private final RestTemplate restTemplate;

  @Value("${api.stock.url}")
  private String BASE_URL;

  @Value("${api.stock.key}")
  private String API_KEY;

  /**
   * @methodName : syncData
   * @date : 2025-03-18 오후 1:49
   * @author : wongil
   * @Description: 지수 데이터 연동
   **/
  public List<SyncJobDto> syncData(IndexDataSyncRequest request, HttpServletRequest httpRequest) {

    List<SyncJobDto> syncJobDtoList = new ArrayList<>();

    List<Index> indexList = getIndexList(request);

    MultiValueMap<String, JsonNode> filteredItems = getFilteredItemMultiValueMap(request, indexList);

    return createDummyResponse(httpRequest, indexList, syncJobDtoList, filteredItems);
  }

  /**
  * @methodName : getIndexList
  * @date : 2025-03-19 오전 10:33
  * @author : wongil
  * @Description: indexInfoIds 조건에 따라 indexList 가져오기
  **/
  private List<Index> getIndexList(IndexDataSyncRequest request) {

    List<Index> indexList = findAllIndexList(request.indexInfoIds());
    if (indexList == null || indexList.isEmpty()) {
      throw new RuntimeException("지수 정보가 없습니다.");
    }

    return indexList;
  }

  /**
  * @methodName : getFilteredItemMultiValueMap
  * @date : 2025-03-19 오전 10:22
  * @author : wongil
  * @Description: {"지수 분류명-지수 이름", jsonNode}로 이루어진 MultiValueMap 생서
  **/
  private MultiValueMap<String, JsonNode> getFilteredItemMultiValueMap(
      IndexDataSyncRequest request, List<Index> indexList) {

    List<String> indexNames = indexList.stream()
        .map(Index::getIndexName)
        .distinct()
        .toList();
    
    return filterItems(indexList,
        getAllItems(getJsonNodeList(request, indexNames)));
  }

  /**
  * @methodName : createDummyResponse
  * @date : 2025-03-19 오전 10:20
  * @author : wongil
  * @Description: 클라이언트에게 dto를 먼저 보내기 위한 메서드
  **/
  private List<SyncJobDto> createDummyResponse(HttpServletRequest httpRequest, List<Index> indexList,
      List<SyncJobDto> syncJobDtoList, MultiValueMap<String, JsonNode> filteredItems) {

    return indexList.stream()
        .flatMap(index -> {

          syncJobDtoList.clear();

          List<JsonNode> items = filteredItems.get(getCsfName(index));
          int size = items.size();

          List<SyncJobDto> dtos = createMockSynDataJob(size, httpRequest, syncJobDtoList);
          List<IndexDataLink> indexDataLinks = saveMockDtoToIndexDataLink(dtos, index, httpRequest);

          // 빈껍데기 IndexVal 객체 생성
          saveDummyIndexVal(size, index);

          setDummyResponseId(index, indexDataLinks, dtos);

          return dtos.stream();
        })
        .toList();
  }


  /**
  * @methodName : saveDummyIndexVal
  * @date : 2025-03-19 오전 10:38
  * @author : wongil
  * @Description: 빈 껍데기 IndexVal 생성
  **/
  private List<IndexVal> saveDummyIndexVal(int size, Index index) {

    List<IndexVal> indexVals = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      IndexVal indexVal = new IndexVal(
          index.getBaseDate(),
          index.getSourceType(),
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          0L,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          index
      );

      indexVals.add(indexVal);
    }

    indexValRepository.saveAll(indexVals);

    return indexVals;
  }

  /**
  * @methodName : setDummyResponseId
  * @date : 2025-03-19 오전 10:30
  * @author : wongil
  * @Description: dummy response와 IndexDataLink id 엮기
  **/
  private void setDummyResponseId(Index index, List<IndexDataLink> indexDataLinks, List<SyncJobDto> dtos) {

    indexDataLinks
        .forEach(link -> {
          dtos
              .forEach(dto -> {
                dto.setId(link.getId());
                dto.setIndexInfoId(index.getId());
              });
        });
  }

  /**
  * @methodName : getCsfName
  * @date : 2025-03-19 오전 10:19
  * @author : wongil
  * @Description: "KOSDAQ시리즈-IT 서비스"로 변환
  **/
  private String getCsfName(Index index) {

    return String.join("-", index.getIndexClassification(), index.getIndexName());
  }

  /**
   * @methodName : getAllItems
   * @date : 2025-03-18 오후 11:43
   * @author : wongil
   * @Description: json 응답 바디에 있는 item들 다 가져오기
   **/
  private List<JsonNode> getAllItems(List<JsonNode> jsonNodeList) {

    List<JsonNode> allItems = new ArrayList<>();

    for (JsonNode json : jsonNodeList) {
      JsonNode items = getItem(json);
      if (items.isArray()) {
        for (int i = 0; i < items.size(); i++) {
          allItems.add(items.get(i));
        }
      }
    }

    return allItems;
  }

  /**
   * @methodName : filterItems
   * @date : 2025-03-18 오후 11:44
   * @author : wongil
   * @Description: index classification로 필터링
   **/
  private MultiValueMap<String, JsonNode> filterItems(List<Index> indexList, List<JsonNode> allItems) {

    MultiValueMap<String, JsonNode> pickItems = new LinkedMultiValueMap<>();

    indexList.forEach(index -> {
      allItems.forEach(item -> {

        String idxCsf = item.path("idxCsf").asText();
        String idxNm = item.path("idxNm").asText();

        if (index.getIndexClassification().equals(idxCsf) && index.getIndexName().equals(idxNm)) {
          pickItems.add(String.join("-", idxCsf, idxNm), item);
        }
      });
    });

    return pickItems;
  }

  /**
   * @methodName : getItem
   * @date : 2025-03-18 오후 9:24
   * @author : wongil
   * @Description: json 응답 바디에서 item 배열만 뽑기
   **/
  private JsonNode getItem(JsonNode json) {
    return json
        .path("response")
        .path("body")
        .path("items")
        .path("item");
  }

  /**
   * @methodName : createMockSynDataJob
   * @date : 2025-03-18 오후 8:02
   * @author : wongil
   * @Description: syncData 개수만큼 dto 미리 생성
   **/
  private List<SyncJobDto> createMockSynDataJob(int syncDataCount, HttpServletRequest httpRequest,
      List<SyncJobDto> syncJobDtoList) {

    for (long indexInfoId = 1L; indexInfoId <= syncDataCount; indexInfoId++) {
      SyncJobDto dto = SyncJobDto.builder()
          .id(null)
          .jobType(ContentType.INDEX_DATA)
          .indexInfoId(indexInfoId)
          .targetDate(null)
          .worker(getUserIp(httpRequest))
          .jobTime(LocalDateTime.now())
          .result("SUCCESS")
          .build();

      syncJobDtoList.add(dto);
    }

    return syncJobDtoList;
  }

  /**
   * @methodName : getJsonNodeList
   * @date : 2025-03-18 오후 7:23
   * @author : wongil
   * @Description: JsonNode stream을 JsonNode List로 변환
   **/
  private List<JsonNode> getJsonNodeList(IndexDataSyncRequest request, List<String> indexNames) {
    return getJsonNodeStream(indexNames, request)
        .filter(items -> items != null && !items.isMissingNode())
        .toList();
  }

  /**
   * @methodName : getJsonNodeStream
   * @date : 2025-03-18 오후 7:22
   * @author : wongil
   * @Description: indexNames로 쿼리해서 JsonNode 얻기
   **/
  private Stream<JsonNode> getJsonNodeStream(List<String> indexNames,
      IndexDataSyncRequest request) {

    return indexNames.stream()
        .map(indexName -> {
          String response = getInfoByBetweenDateOpenApi(request, indexName);

          return SyncJobUtils.findItems(response);

        });
  }


  /**
   * @methodName : saveMockDtoToIndexDataLink
   * @date : 2025-03-18 오후 6:21
   * @author : wongil
   * @Description: dto와 index로 indexDataLink 객체 생성
   **/
  private List<IndexDataLink> saveMockDtoToIndexDataLink(List<SyncJobDto> dtos,
      Index index, HttpServletRequest request) {

    List<IndexDataLink> links = new ArrayList<>();

    dtos
        .forEach(dto -> {

          IndexDataLink indexDataLink = new IndexDataLink(
              null,
              ContentType.INDEX_DATA,
              index.getBaseDate(),
              getUserIp(request),
              LocalDateTime.now(),
              true,
              index
          );

          links.add(indexDataLink);
        });

    return indexDataLinkRepository.saveAll(links);
  }


  /**
   * @methodName : getBetweenDateInfoByOpenApi
   * @date : 2025-03-18 오후 2:18
   * @author : wongil
   * @Description: beginBasDt과 endBasDt 사이의 날짜 데이터 가져오기
   **/
  private String getInfoByBetweenDateOpenApi(IndexDataSyncRequest request, String indexName) {

    String encodedName = encodeName(indexName);

    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
        .queryParam("serviceKey", API_KEY)
        .queryParam("resultType", "json")
        .queryParam("beginBasDt", convertToStringDateFormat(request.baseDateFrom()))
        .queryParam("endBasDt", convertToStringDateFormat(request.baseDateTo()))
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 100)
        .queryParam("idxNm", encodedName);

    return getResponseBody(builder);
  }

  /**
   * @methodName : encodeName
   * @date : 2025-03-18 오후 7:34
   * @author : wongil
   * @Description: 한글 이름만 인코딩
   **/
  private String encodeName(String indexName) {
    String encodedName = null;
    try {
      encodedName = URLEncoder.encode(indexName, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("idxNm encoding fail!!");
    }
    return encodedName;
  }

  /**
   * @methodName : getResponseBody
   * @date : 2025-03-18 오후 5:57
   * @author : wongil
   * @Description: json response를 받기 위해 변환
   **/
  private String getResponseBody(UriComponentsBuilder builder) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.set("Content-Type", "application/json; charset=UTF-8");

    HttpEntity<?> entity = new HttpEntity<>(headers);

    String uriString = builder.build(false).toUriString();
    URI uri = URI.create(uriString);

    ResponseEntity<String> response = restTemplate.exchange(
        uri,
        HttpMethod.GET,
        entity,
        String.class
    );

    return response.getBody();
  }

  /**
   * @methodName : convertToStringDateFormat
   * @date : 2025-03-18 오후 5:57
   * @author : wongil
   * @Description: "2024-10-10"으로 된걸 20241010으로 변환
   **/
  private String convertToStringDateFormat(@NotNull LocalDate localDate) {
    return String.join("", localDate.toString().split("-"));
  }

  /**
   * @methodName : findIndexListByIds
   * @date : 2025-03-18 오후 2:09
   * @author : wongil
   * @Description: 사용자가 보낸 List<Integer> indexInfoIds 이걸 받아서 index 찾기 infoIds가 없으면 모든 지수를 검색
   **/
  private List<Index> findAllIndexList(List<Integer> indexInfoIds) {
    if (indexInfoIds == null || indexInfoIds.isEmpty()) {
      return indexRepository.findAll();
    }

    return indexRepository.findAllByIdIn(indexInfoIds);
  }
}
