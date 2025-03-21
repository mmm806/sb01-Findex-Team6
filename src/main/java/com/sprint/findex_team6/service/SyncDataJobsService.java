package com.sprint.findex_team6.service;

import static com.sprint.findex_team6.service.util.SyncJobUtils.getUserIp;

import com.fasterxml.jackson.databind.JsonNode;
import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.IndexDataSyncRequest;
import com.sprint.findex_team6.entity.ContentType;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexDataLink;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.exception.syncjobs.DuplicateIndexException;
import com.sprint.findex_team6.exception.syncjobs.NotFoundIndeValException;
import com.sprint.findex_team6.exception.syncjobs.NotFoundIndexException;
import com.sprint.findex_team6.exception.syncjobs.NotFoundItemException;
import com.sprint.findex_team6.exception.syncjobs.SyncFailedException;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
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

    List<JsonNode> items = getItems(request, indexList);

    MultiValueMap<String, JsonNode> filteredItems = getFilteredItemMultiValueMap(request, indexList);
    List<SyncJobDto> dummyResponse = createDummyResponse(httpRequest, indexList, syncJobDtoList,
        filteredItems);

    // 비동기로 데이터 갱신
    schedulerAsyncIndexData(indexList, dummyResponse, items);

    return dummyResponse;
  }

  /**
  * @methodName : getItems
  * @date : 2025-03-21 오전 9:49
  * @author : wongil
  * @Description: OPENAPI에서 지수 분류명으로 데이터 가져오기
  **/
  private List<JsonNode> getItems(IndexDataSyncRequest request, List<Index> indexList) {
    List<String> names = indexList.stream()
        .map(Index::getIndexName)
        .toList();
    return getJsonNodeList(request, names);
  }

  /**
  * @methodName : schedulerAsyncIndexData
  * @date : 2025-03-19 오후 1:42
  * @author : wongil
  * @Description: 비동기 작업을 위한 스케줄러
  **/
  private void schedulerAsyncIndexData(List<Index> indexList, List<SyncJobDto> jobDtoList, List<JsonNode> items) {

    CompletableFuture.runAsync(() -> {
      try {
        process(indexList, jobDtoList, items);
      } catch (SyncFailedException e) {
        log.error("Async error!!", e);
      }
    });
  }

  /**
  * @methodName : process
  * @date : 2025-03-19 오후 1:42
  * @author : wongil
  * @Description: 실제 비동기 작업
  **/
  private void process(List<Index> indexList, List<SyncJobDto> jobDtoList, List<JsonNode> items) {
    List<IndexVal> indexVals = findIndexValListByIndexIdAndTargetDate(indexList, jobDtoList);

    List<JsonNode> filteredItems = filterItemsByClassificationName(indexList, getAllItems(items));

    for (int i = 0; i < indexVals.size() && i < filteredItems.size(); i++) {
      IndexVal indexVal = indexVals.get(i);
      Double mkp = filteredItems.get(i).path("mkp").asDouble(); // 시가
      Double clpr = filteredItems.get(i).path("clpr").asDouble();// 종가
      Double hipr = filteredItems.get(i).path("hipr").asDouble();// 고가
      Double lopr = filteredItems.get(i).path("lopr").asDouble();// 저가
      Double vs = filteredItems.get(i).path("vs").asDouble();// 대비
      Double fltRt = filteredItems.get(i).path("fltRt").asDouble();// 등락률
      Long trqu = filteredItems.get(i).path("trqu").asLong();// 거래량
      Double trPrc = filteredItems.get(i).path("trPrc").asDouble();// 거래대금
      Double lstgMrktTotAmt = filteredItems.get(i).path("lstgMrktTotAmt").asDouble();// 상장시가총액

      IndexVal changedIndexVal = indexVal.changeData(mkp, clpr, hipr, lopr, vs, fltRt, trqu, trPrc,
          lstgMrktTotAmt);

      indexValRepository.save(changedIndexVal);
    }

  }

  /**
  * @methodName : filterItemsByClassificationName
  * @date : 2025-03-19 오후 2:25
  * @author : wongil
  * @Description: 지수 분류명으로 특정 item만 필터링
  **/
  private List<JsonNode> filterItemsByClassificationName(List<Index> indexList, List<JsonNode> allItems) {
    List<JsonNode> itemList = indexList.stream()
        .flatMap(index ->
            allItems.stream()
                .filter(item -> item.path("idxCsf").asText().equals(index.getIndexClassification()))
        )
        .toList();

    if (itemList.isEmpty()) {
      throw new NotFoundItemException();
    }

    return itemList;
  }

  /**
  * @methodName : findIndexValListByIndexIdAndTargetDate
  * @date : 2025-03-19 오후 2:11
  * @author : wongil
  * @Description: index.id와 targetDate로 해당하는 IndexVal 찾기
  **/
  private List<IndexVal> findIndexValListByIndexIdAndTargetDate(List<Index> indexList, List<SyncJobDto> jobDtoList) {
    List<IndexVal> indexValList = indexList.stream()
        .flatMap(index ->
            jobDtoList.stream()
                .flatMap(dto ->
                    indexValRepository.findAllByIndex_IdAndBaseDate(index.getId(),
                            dto.getTargetDate())
                        .stream())
        )
        .toList();

    if (indexList.isEmpty()) {
      throw new NotFoundIndeValException();
    }

    return indexValList;
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
      throw new NotFoundIndexException("지수 정보가 없습니다.");
    }

    return indexList;
  }

  /**
  * @methodName : getFilteredItemMultiValueMap
  * @date : 2025-03-19 오전 10:22
  * @author : wongil
  * @Description: {"지수 분류명-지수 이름", jsonNode}로 이루어진 MultiValueMap 생성
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

          List<String> baseDateList = items.stream()
              .map(item -> item.path("basDt").asText())
              .toList();

          List<SyncJobDto> dtos = createMockSynDataJob(size, httpRequest, syncJobDtoList, baseDateList);
          List<IndexDataLink> indexDataLinks = saveMockDtoToIndexDataLink(dtos, index, httpRequest);

          // 빈껍데기 IndexVal 객체 생성
          saveDummyIndexVal(size, indexDataLinks, index);

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
  private List<IndexVal> saveDummyIndexVal(int size, List<IndexDataLink> indexDataLinks, Index index) {

    List<IndexVal> indexVals = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      IndexVal indexVal = new IndexVal(
          indexDataLinks.get(i).getTargetDate(),
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

    List<Long> valIndexIds = indexVals.stream()
        .map(val -> val.getIndex().getId())
        .toList();

    boolean isExistsId = indexValRepository.existsByIndex_IdIn(valIndexIds);
    if (isExistsId) {
      throw new DuplicateIndexException();
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
   * @Description: json 응답 바디에 있는 모든 item 다 가져오기
   **/
  private List<JsonNode> getAllItems(List<JsonNode> jsonNodeList) {

    List<JsonNode> allItems = new ArrayList<>();

    for (JsonNode json : jsonNodeList) {
      JsonNode item = getItem(json);

      if (item.isArray()) {
        for (int i = 0; i < item.size(); i++) {
          allItems.add(item.get(i));
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
    JsonNode path = json
        .path("response")
        .path("body")
        .path("items")
        .path("item");

    if (path == null) {
      throw new NotFoundItemException();
    }


    return path;
  }

  /**
   * @methodName : createMockSynDataJob
   * @date : 2025-03-18 오후 8:02
   * @author : wongil
   * @Description: syncData 개수만큼 dto 미리 생성
   **/
  private List<SyncJobDto> createMockSynDataJob(int syncDataCount, HttpServletRequest httpRequest,
      List<SyncJobDto> syncJobDtoList, List<String> baseDateList) {

    Map<Long, LocalDate> baseDateMap = new LinkedHashMap<>();
    AtomicLong counter = new AtomicLong(1);

    baseDateList.forEach(dateString -> {
      LocalDate localDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
      baseDateMap.put(counter.getAndIncrement(), localDate);
    });

    for (long indexInfoId = 1L; indexInfoId <= syncDataCount; indexInfoId++) {
      SyncJobDto dto = SyncJobDto.builder()
          .id(null)
          .jobType(ContentType.INDEX_DATA)
          .indexInfoId(indexInfoId)
          .targetDate(baseDateMap.get(indexInfoId))
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
              dto.getTargetDate(),
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
