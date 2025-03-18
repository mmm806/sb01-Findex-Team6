package com.sprint.findex_team6.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.entity.ContentType;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexDataLink;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.exception.NotFoundException;
import com.sprint.findex_team6.repository.IndexDataLinkRepository;
import com.sprint.findex_team6.repository.IndexRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SyncInfoJobsService {

  private final IndexDataLinkRepository indexDataLinkRepository;
  private final IndexRepository indexRepository;

  private final PlatformTransactionManager transactionManager;
  private final RestTemplate restTemplate;

  private final int WHAT_DAYS_FROM = 3;

  @Value("${api.stock.url}")
  private String BASE_URL;

  @Value("${api.stock.key}")
  private String API_KEY;

  /**
   * @methodName : syncInfo
   * @date : 2025-03-17 오후 10:32
   * @author : wongil
   * @Description: 지수 정보 연동
   **/
  public List<SyncJobDto> syncInfo(HttpServletRequest request) {

    List<SyncJobDto> syncIndexInfoJobDtoList = new ArrayList<>();

    String response = getAllInfosByCallOpenApi();
    JsonNode items = findItems(response);

    List<SyncJobDto> mockList = createMockSyncInfoJobResponse(items, request, syncIndexInfoJobDtoList);
    List<IndexDataLink> indexDataLinks = saveMockDtos(mockList);

    schedulerAsyncIndexInfo(indexDataLinks);

    return mockList;
  }

  /**
   * @methodName : schedulerAysncIndexInfo
   * @date : 2025-03-18 오전 10:29
   * @author : wongil
   * @Description: IndexInfo를 저장할 비동기 스케줄러
   **/
  private void schedulerAsyncIndexInfo(List<IndexDataLink> indexDataLinks) {

    List<Long> indexDataLinkIds = indexDataLinks.stream()
        .map(IndexDataLink::getId)
        .toList();

    CompletableFuture.runAsync(() -> {
      try {
        process(indexDataLinkIds);
      } catch (Exception e) {
        log.error("Async error!!", e);
      }
    });
  }

  /**
   * @methodName : process
   * @date : 2025-03-18 오전 10:33
   * @author : wongil
   * @Description: 실제 저장 프로세스
   **/
  @Transactional(readOnly = true)
  protected void process(List<Long> indexDataLinkIds) {

    // 트랜잭션을 직접 관리
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    String response = getAllInfosByCallOpenApi();
    JsonNode items = findItems(response);

    int numOfRows = getNumOfRows(items);
    int totalPages = getTotalPages(items, numOfRows);

    transactionTemplate.execute(status -> {
      firstPageFetch(response, indexDataLinkIds);
      morePagesFetch(totalPages, numOfRows, indexDataLinkIds);
      return null;
    });
  }

  /**
   * @methodName : saveMockDtos
   * @date : 2025-03-18 오전 10:16
   * @author : wongil
   * @Description: SyncJob Mock을 실제 IndexDataLink Respository에 저장
   **/
  private List<IndexDataLink> saveMockDtos(List<SyncJobDto> mockDtoList) {

    List<IndexDataLink> indexDataLinkList = mockDtoList.stream()
        .map(mock -> new IndexDataLink(
            null,
            mock.getJobType(),
            LocalDate.now(),
            mock.getWorker(),
            mock.getJobTime(),
            isSuccessOrFail(mock.getResult()),
            null
        ))
        .toList();

    return getIndexDataLinks(mockDtoList, indexDataLinkList);
  }

  /**
  * @methodName : getIndexDataLinks
  * @date : 2025-03-18 오후 1:34
  * @author : wongil
  * @Description: IndexDataLink 실제로 저장하고 id 주입
  **/
  private List<IndexDataLink> getIndexDataLinks(List<SyncJobDto> mockDtoList,
      List<IndexDataLink> indexDataLinkList) {
    List<IndexDataLink> indexDataLinks = indexDataLinkRepository.saveAll(indexDataLinkList);

    for (int i = 0; i < mockDtoList.size() && i < indexDataLinks.size(); i++) {
      mockDtoList.get(i).setId(indexDataLinks.get(i).getId());
    }
    return indexDataLinks;
  }

  /**
   * @methodName : createMockSyncJobResponse
   * @date : 2025-03-18 오전 10:06
   * @author : wongil
   * @Description: IndexInfo 응답을 위한 가짜 응답 생성
   **/
  private List<SyncJobDto> createMockSyncInfoJobResponse(JsonNode items, HttpServletRequest request,
      List<SyncJobDto> syncIndexInfoJobDtoList) {

    int totalCount = getTotalCount(items);

    for (long indexInfoId = 1L; indexInfoId <= totalCount; indexInfoId++) {
      SyncJobDto dto = SyncJobDto.builder()
          .id(null)
          .jobType(ContentType.INDEX_INFO)
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
   * @date : 2025-03-18 오전 10:07
   * @author : wongil
   * @Description: worker IP 가져오기
   **/
  private String getUserIp(HttpServletRequest request) {
    return request.getRemoteAddr();
  }

  /**
   * @methodName : saveIndexDataLink
   * @date : 2025-03-17 오후 11:20
   * @author : wongil
   * @Description: dto -> IndexDataLink로 변환 후 repository에 저장
   **/
  private void saveIndexDataLink(List<SyncJobDto> syncIndexInfoJobDtoList) {
    for (SyncJobDto dto : syncIndexInfoJobDtoList) {
      save(dto);

      Long savedId = getSavedIndexDataLinkId(dto);

      if (savedId != null) {
        dto.setId(savedId);
      }
    }
  }

  /**
   * @methodName : getSavedIndexDataLinkId
   * @date : 2025-03-17 오후 11:16
   * @author : wongil
   * @Description: 저장된 Index Data Link id 가져오기
   **/
  private Long getSavedIndexDataLinkId(SyncJobDto dto) {
    List<IndexDataLink> indexDataLink = indexDataLinkRepository.findByIndex_IdAndTargetDateAndJobTime(
        dto.getIndexInfoId(),
        dto.getTargetDate(), dto.getJobTime());

    return !indexDataLink.isEmpty() ? indexDataLink.get(0).getId() : null;
  }

  /**
   * @methodName : save
   * @date : 2025-03-17 오후 11:03
   * @author : wongil
   * @Description: SyncJobDto -> IndexDatLink로 변환 후 repository에 저장
   **/
  private void save(SyncJobDto dto) {
    Boolean result = isSuccessOrFail(dto.getResult());
    Index index = findIndex(dto.getIndexInfoId());

    IndexDataLink indexDataLink = new IndexDataLink(
        null,
        dto.getJobType(),
        dto.getTargetDate(),
        dto.getWorker(),
        dto.getJobTime(),
        result,
        index
    );

    indexDataLinkRepository.save(indexDataLink);
  }

  /**
   * @methodName : findIndex
   * @date : 2025-03-17 오후 11:06
   * @author : wongil
   * @Description: indexRepository에서 id로 꺼내기
   **/
  private Index findIndex(Long indexInfoId) {
    return indexRepository.findById(indexInfoId)
        .orElseThrow(() -> new NotFoundException("Not found Index!!"));
  }

  /**
   * @methodName : isSuccessOrFail
   * @date : 2025-03-17 오후 11:05
   * @author : wongil
   * @Description: result String -> db에는 boolean으로 들어가야함 변환 작업
   **/
  private Boolean isSuccessOrFail(String result) {
    return !result.equals("FAILED");
  }

  /**
   * @methodName : morePagesFetch
   * @date : 2025-03-17 오후 11:01
   * @author : wongil
   * @Description: 2페이지부터 순회하며 데이터 뽑기
   **/
  private void morePagesFetch(int totalPages, int numOfRows,
      List<Long> indexDataLinkIds) {

    if (totalPages > 1) {
      for (int pageNumber = 2; pageNumber <= totalPages; pageNumber++) {
        fetchInfo(indexDataLinkIds, pageNumber, numOfRows);
      }
    }
  }

  /**
   * @methodName : firstPageFetch
   * @date : 2025-03-17 오후 11:00
   * @author : wongil
   * @Description: 1페이지 처리
   **/
  private void firstPageFetch(String response, List<Long> indexDataLinkIds) {

    saveIndex(getItems(response), indexDataLinkIds, 0);
  }

  /**
   * @methodName : fetchInfo
   * @date : 2025-03-18 오전 9:14
   * @author : wongil
   * @Description:
   **/
  private void fetchInfo(List<Long> indexDataLinkIds, int pageNumber,
      int numOfRows) {

    int offset = (pageNumber - 1) * numOfRows;

    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
        .queryParam("serviceKey", API_KEY)
        .queryParam("resultType", "json")
        .queryParam("beginBasDt", convertToStringDateFormat(LocalDate.now().minusDays(WHAT_DAYS_FROM)))
        .queryParam("endBasDt", convertToStringDateFormat(LocalDate.now()))
        .queryParam("pageNo", pageNumber)
        .queryParam("numOfRows", numOfRows);

    String responseBody = getResponseBody(builder);
    JsonNode items = getItems(responseBody);

    saveIndex(items, indexDataLinkIds, offset);
  }

  /**
   * @methodName : saveIndex
   * @date : 2025-03-17 오후 10:40
   * @author : wongil
   * @Description: items가 배열(item)인 경우에만 배열 내 필드 파싱해서 데이터 저장
   **/
  private void saveIndex(JsonNode items, List<Long> indexDataLinkIds, int offset) {
    if (items.isArray()) {
      saveIndexInfo(items, indexDataLinkIds, offset);
    }
  }

  /**
   * @methodName : saveIndexInfo
   * @date : 2025-03-17 오후 10:41
   * @author : wongil
   * @Description: items에서 item 꺼내 실제 Index 만들기
   **/
  private void saveIndexInfo(JsonNode items, List<Long> indexDataLinkIds, int offset) {
    List<JsonNode> itemList = sortJsonNodeList(items);

    List<Index> indexList = new ArrayList<>();
    List<IndexDataLink> linksToUpdate = new ArrayList<>();

    // IndexDatRepository에서 id로 찾고, Index 객체 생성 후 리스트에 저장
    for (int i = 0; i < itemList.size() && (offset + i) < indexDataLinkIds.size(); i++) {
      JsonNode item = itemList.get(i);
      Long linkId = indexDataLinkIds.get(offset + i);

      IndexDataLink freshLink = indexDataLinkRepository.findById(linkId)
          .orElseThrow(() -> new RuntimeException("IndexDataLink not found: " + linkId));
      linksToUpdate.add(freshLink);

      Index index = createIndex(item);
      indexList.add(index);
    }

    // 한번에 index 저장
    List<Index> savedIndexes = indexRepository.saveAll(indexList);

    // 지수 연동 데이트 업데이트
    for (int i = 0; i < savedIndexes.size(); i++) {
      Index savedIndex = savedIndexes.get(i);
      IndexDataLink link = linksToUpdate.get(i);

      link.changeTargetDateAndIndex(savedIndex.getBaseDate(), savedIndex);
    }

    indexDataLinkRepository.saveAll(linksToUpdate);
  }

  /**
  * @methodName : sortJsonNodeList
  * @date : 2025-03-18 오후 3:25
  * @author : wongil
  * @Description: 지수명으로 먼저 정렬하고 같으면 날짜로 정렬
  **/
  private List<JsonNode> sortJsonNodeList(JsonNode items) {
    List<JsonNode> itemList = new ArrayList<>();
    for (JsonNode item : items) {
      itemList.add(item);
    }

    itemList.sort(Comparator
        .<JsonNode, String>comparing(item -> item.path("idxCsf").asText())
        .thenComparing(item -> item.path("basPntm").asText())
    );
    return itemList;
  }

  /**
   * @methodName : createSyncJobDtoList
   * @date : 2025-03-17 오후 10:47
   * @author : wongil
   * @Description: SyncJobDto list에 값 넣기
   **/
  private void createSyncJobDtoList(Index savedIndex, List<SyncJobDto> syncIndexInfoJobDtoList) {
    SyncJobDto dto = SyncJobDto.builder()
        .id(null)
        .jobType(ContentType.INDEX_INFO)
        .jobTime(LocalDateTime.now())
        .targetDate(savedIndex.getBaseDate())
        .indexInfoId(savedIndex.getId())
        .result("SUCCESS")
        .worker("system")
        .build();

    syncIndexInfoJobDtoList.add(dto);
  }

  /**
   * @methodName : createIndex
   * @date : 2025-03-17 오후 10:42
   * @author : wongil
   * @Description: 실제 Index 생성
   **/
  private Index createIndex(JsonNode item) {
    String indexClassification = item.path("idxCsf").asText();
    String idxNm = item.path("idxNm").asText();
    int employedItemsCount = item.path("epyItmsCnt").asInt();
    String baseDate = item.path("basPntm").asText();
    String baseIndex = item.path("basIdx").asText();
    SourceType sourceType = SourceType.OPEN_API;
    Boolean favorite = false;

    return new Index(
        indexClassification,
        idxNm,
        employedItemsCount,
        LocalDate.parse(baseDate, DateTimeFormatter.ofPattern("yyyyMMdd")),
        new BigDecimal(baseIndex),
        sourceType,
        favorite
    );
  }

  /**
   * @methodName : getItems
   * @date : 2025-03-17 오후 10:38
   * @author : wongil
   * @Description: api reponse body의 item 배열만 뽑기
   **/
  private JsonNode getItems(String response) {
    return findItems(response)
        .path("response")
        .path("body")
        .path("items")
        .path("item");
  }

  /**
   * @methodName : getTotalPages
   * @date : 2025-03-17 오후 10:37
   * @author : wongil
   * @Description: 총 페이지 수 구하기
   **/
  private int getTotalPages(JsonNode items, int numOfRows) {
    int totalCount = getTotalCount(items);

    if (numOfRows <= 0) {
      numOfRows = 100;
    }

    return (int) Math.ceil((double) totalCount / numOfRows);
  }

  /**
   * @methodName : getTotalCount
   * @date : 2025-03-17 오후 10:36
   * @author : wongil
   * @Description: API 응답 바디에서 totalCount만 뽑기
   **/
  private int getTotalCount(JsonNode items) {
    return items
        .path("response")
        .path("body")
        .path("totalCount")
        .asInt();
  }

  /**
   * @methodName : getNumOfRows
   * @date : 2025-03-17 오후 10:32
   * @author : wongil
   * @Description: API 응답 바디에서 numOfRows 뽑기
   **/
  private int getNumOfRows(JsonNode items) {
    return items
        .path("response")
        .path("body")
        .path("numOfRows")
        .asInt();
  }

  /**
   * @methodName : findItems
   * @date : 2025-03-17 오후 10:30
   * @author : wongil
   * @Description: API 응답 결과 파싱해서 JsonNode 타입으로 반환
   **/
  private JsonNode findItems(String response) {
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      return objectMapper.readTree(response);
    } catch (JsonProcessingException e) {
      log.error("API response data: {}", response);
      throw new RuntimeException(e);
    }
  }

  /**
   * @methodName : getAllInfosByCallOpenApi
   * @date : 2025-03-17 오후 10:29
   * @author : wongil
   * @Description: 지수 정보를 가져오기 위한 코드 현재 날짜로부터 -3일까지의 데이터만 긁어옴
   **/
  private String getAllInfosByCallOpenApi() {

    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
        .queryParam("serviceKey", API_KEY)
        .queryParam("resultType", "json")
        .queryParam("beginBasDt", convertToStringDateFormat(LocalDate.now().minusDays(WHAT_DAYS_FROM)))
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 100);

    return getResponseBody(builder);
  }

  /**
   * @methodName : getResponseBody
   * @date : 2025-03-17 오후 10:58
   * @author : wongil
   * @Description: json body 뽑기
   **/
  private String getResponseBody(UriComponentsBuilder builder) {

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    httpHeaders.set("Content-Type", "application/json; charset=UTF-8");

    HttpEntity<?> entity = new HttpEntity<>(httpHeaders);

    String uriString = builder.build(true).toUriString();
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
   * @date : 2025-03-17 오후 10:30
   * @author : wongil
   * @Description: LocalDate을 "20240731" 이런 형식으로 변환
   **/
  private String convertToStringDateFormat(LocalDate localDate) {
    return String.join("", localDate.toString().split("-"));
  }

}
