package com.sprint.findex_team6.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.entity.ContentType;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexDataLink;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.exception.syncjobs.FailedCallOpenApiException;
import com.sprint.findex_team6.exception.syncjobs.NotFoundIndexException;
import com.sprint.findex_team6.exception.syncjobs.NotFoundItemException;
import com.sprint.findex_team6.exception.syncjobs.SyncFailedException;
import com.sprint.findex_team6.repository.IndexDataLinkRepository;
import com.sprint.findex_team6.repository.IndexRepository;
import com.sprint.findex_team6.service.util.SyncJobUtils;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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

  private final int WHAT_DAYS_FROM = 1;

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

    String response = getAllInfosByCallOpenApi();
    JsonNode items = SyncJobUtils.findItems(response);

    int totalCount = SyncJobUtils.getTotalCount(items);
    if (totalCount == 0) {
      return returnAlreadySyncJobData();
    }

    List<SyncJobDto> isDuplicated = validDuplicatedIndex(response);
    if (isDuplicated != null) {
      return isDuplicated;
    }

    int numOfRows = SyncJobUtils.getNumOfRows(items);
    int totalPages = SyncJobUtils.getTotalPages(items);


    // index id 값을 얻기 위해 더미로 저장
    List<Index> indexList = indexRepository.saveAll(createDummyIndex(totalCount));

    // 더미 index의 id 값으로 dto 만들고 저장
    List<SyncJobDto> mockList = createMockSyncInfoJobResponse(indexList, request);
    saveMockDtos(mockList, indexList);

    schedulerAsyncIndexInfo(indexList, totalPages, numOfRows);

    return mockList;
  }

  /**
   * @methodName : returnAlreadySyncJobData
   * @date : 2025-03-21 오전 10:43
   * @author : wongil
   * @Description: 만약 OPEN API 호출 시 결과가 없고 db에 index data link가 있으면 바로 반환
   **/
  private List<SyncJobDto> returnAlreadySyncJobData() {
    List<IndexDataLink> links = indexDataLinkRepository.findAll();

    if (!links.isEmpty()) {
      return links.stream()
          .map(link ->
              SyncJobDto.builder()
                  .id(link.getId())
                  .indexInfoId(link.getIndex().getId())
                  .jobType(link.getJobType())
                  .targetDate(null)
                  .worker(link.getWorker())
                  .jobTime(link.getJobTime())
                  .result(link.getResult() ? "SUCCESS" : "FAILED")
                  .build()
          )
          .toList();
    }

    return null;
  }

  /**
   * @methodName : validDuplicatedIndex
   * @date : 2025-03-21 오전 10:26
   * @author : wongil
   * @Description: 지수 정보 중복 저장 되는거 방지
   **/
  private List<SyncJobDto> validDuplicatedIndex(String response) {
    JsonNode items = getItems(response);

    List<IndexDataLink> links = indexDataLinkRepository.findAll();

    for (JsonNode item : items) {
      String classificationName = item.path("idxCsf").asText();
      String indexName = item.path("idxNm").asText();
      String basPntm = item.path("basPntm").asText();

      for (IndexDataLink link : links) {
        if (link.getIndex().getIndexClassification().equals(classificationName) &&
            link.getIndex().getIndexName().equals(indexName) &&
            link.getIndex().getBaseDate()
                .equals(LocalDate.parse(basPntm, DateTimeFormatter.ofPattern("yyyyMMdd")))) {

          return returnAlreadySyncJobData();
        }
      }
    }

    return null;

//    List<Index> indexList = indexRepository.findAll();
//
//    for (JsonNode item : items) {
//      String classificationName = item.path("idxCsf").asText();
//      String indexName = item.path("idxNm").asText();
//      String basPntm = item.path("basPntm").asText();
//
//      indexList.forEach(index -> {
//            if (index.getIndexClassification().equals(classificationName) &&
//                index.getIndexName().equals(indexName) &&
//                index.getBaseDate()
//                    .equals(LocalDate.parse(basPntm, DateTimeFormatter.ofPattern("yyyyMMdd")))) {
//
//              throw new DuplicateIndexException();
//            }
//          });
//    }

  }

  /**
   * @methodName : createDummyIndex
   * @date : 2025-03-18 오후 4:20
   * @author : wongil
   * @Description: 더미 index 객체 생성
   **/
  private List<Index> createDummyIndex(int totalCount) {
    List<Index> dummyList = new ArrayList<>();

    for (int i = 0; i < totalCount; i++) {
      Index index = new Index(
          "DUMMY" + i,
          "DUMMY" + i,
          0,
          LocalDate.now(),
          new BigDecimal(1),
          SourceType.OPEN_API,
          false
      );

      dummyList.add(index);
    }

    return dummyList;
  }

  /**
   * @methodName : schedulerAysncIndexInfo
   * @date : 2025-03-18 오전 10:29
   * @author : wongil
   * @Description: IndexInfo를 저장할 비동기 스케줄러
   **/
  private void schedulerAsyncIndexInfo(List<Index> indexList, int totalPages, int numOfRows) {

    List<Long> indexIds = indexList.stream()
        .map(Index::getId)
        .toList();

    CompletableFuture.runAsync(() -> {
      try {
        process(indexIds, totalPages, numOfRows);
      } catch (SyncFailedException e) {
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
  protected void process(List<Long> indexIds, int totalPages, int numOfRows) {

    // 트랜잭션을 직접 관리
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    String response = getAllInfosByCallOpenApi();

    if (response == null) {
      throw new FailedCallOpenApiException();
    }

    transactionTemplate.execute(status -> {

      firstPageFetch(response, indexIds, 0);
      morePagesFetch(totalPages, numOfRows, indexIds);

      return null;
    });
  }

  /**
   * @methodName : saveMockDtos
   * @date : 2025-03-18 오전 10:16
   * @author : wongil
   * @Description: SyncJob Mock을 실제 IndexDataLink Respository에 저장
   **/
  private List<IndexDataLink> saveMockDtos(List<SyncJobDto> mockDtoList, List<Index> indexList) {

    Map<Long, Index> indexMap = indexList.stream()
        .collect(Collectors.toMap(Index::getId, index -> index));

    List<IndexDataLink> indexDataLinkList = new ArrayList<>();

    for (SyncJobDto mock : mockDtoList) {

      // mock의 indexInfoId로 해당 Index 객체 찾기
      Index index = indexMap.get(mock.getIndexInfoId());

      if (index != null) {
        IndexDataLink link = new IndexDataLink(
            null,
            mock.getJobType(),
            LocalDate.now(),
            mock.getWorker(),
            mock.getJobTime(),
            isSuccessOrFail(mock.getResult()),
            index
        );
        indexDataLinkList.add(link);
      }
    }

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
  private List<SyncJobDto> createMockSyncInfoJobResponse(List<Index> indexList,
      HttpServletRequest request) {
    List<SyncJobDto> syncJobDtoList = new ArrayList<>();

    for (Index index : indexList) {
      SyncJobDto dto = SyncJobDto.builder()
          .id(null)
          .jobType(ContentType.INDEX_INFO)
          .indexInfoId(index.getId())
          .targetDate(index.getBaseDate())
          .worker(SyncJobUtils.getUserIp(request))
          .jobTime(LocalDateTime.now())
          .result("SUCCESS")
          .build();

      syncJobDtoList.add(dto);
    }

    return syncJobDtoList;
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
      List<Long> indexIds) {

    if (totalPages > 1) {
      for (int pageNumber = 2; pageNumber <= totalPages; pageNumber++) {
        fetchInfo(indexIds, pageNumber, numOfRows);
      }
    }
  }

  /**
   * @methodName : firstPageFetch
   * @date : 2025-03-17 오후 11:00
   * @author : wongil
   * @Description: 1페이지 처리
   **/
  private void firstPageFetch(String response, List<Long> indexDataLinkIds, int offset) {

    saveIndex(getItems(response), indexDataLinkIds, offset);
  }

  /**
   * @methodName : fetchInfo
   * @date : 2025-03-18 오전 9:14
   * @author : wongil
   * @Description:
   **/
  private void fetchInfo(List<Long> indexIds, int pageNumber,
      int numOfRows) {

    int offset = (pageNumber - 1) * numOfRows;

    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
        .queryParam("serviceKey", API_KEY)
        .queryParam("resultType", "json")
        .queryParam("beginBasDt",
            convertToStringDateFormat(LocalDate.now().minusDays(WHAT_DAYS_FROM)))
        .queryParam("endBasDt", convertToStringDateFormat(LocalDate.now()))
        .queryParam("pageNo", pageNumber)
        .queryParam("numOfRows", numOfRows);

    String responseBody = getResponseBody(builder);
    JsonNode items = getItems(responseBody);

    saveIndex(items, indexIds, offset);
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

    List<IndexDataLink> updateIndexDataLinkList = new ArrayList<>();

    // IndexDatRepository에서 id로 찾고, Index 객체 생성 후 리스트에 저장
    for (int i = 0; i < itemList.size() && (offset + i) < indexDataLinkIds.size(); i++) {
      JsonNode item = itemList.get(i);
      Long indexId = indexDataLinkIds.get(offset + i);

      Index index = indexRepository.findById(indexId)
          .orElseThrow(NotFoundIndexException::new);

      // 실제 데이터로 업데이트
      updateIndex(index, item);
      indexRepository.save(index);

      // 관련 IndexDataLink 찾기
      List<IndexDataLink> links = indexDataLinkRepository.findByIndex_Id(indexId);
      if (!links.isEmpty()) {
        for (IndexDataLink link : links) {
          link.changeTargetDateAndIndex(index.getBaseDate(), index);
          updateIndexDataLinkList.add(link);
        }
      }
    }

    if (!updateIndexDataLinkList.isEmpty()) {
      indexDataLinkRepository.saveAll(updateIndexDataLinkList);
    }
  }

  /**
   * @methodName : updateIndex
   * @date : 2025-03-18 오후 5:12
   * @author : wongil
   * @Description: 더미 index를 실제 데이터로 업데이트
   **/
  private void updateIndex(Index index, JsonNode item) {
    String indexClassification = item.path("idxCsf").asText();
    String idxNm = item.path("idxNm").asText();
    int employedItemsCount = item.path("epyItmsCnt").asInt();
    String baseDate = item.path("basPntm").asText();
    String baseIndex = item.path("basIdx").asText();
    LocalDate parsedBaseDate = LocalDate.parse(baseDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

    index.updateInfo(indexClassification, idxNm, employedItemsCount, parsedBaseDate,
        new BigDecimal(baseIndex));
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
        .thenComparing(item -> item.path("idxNm").asText())
        .thenComparing(item -> item.path("basPntm").asText())
    );
    return itemList;
  }

  /**
   * @methodName : getItems
   * @date : 2025-03-17 오후 10:38
   * @author : wongil
   * @Description: api reponse body의 item 배열만 뽑기
   **/
  private JsonNode getItems(String response) {
    JsonNode path = SyncJobUtils.findItems(response)
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
   * @methodName : getAllInfosByCallOpenApi
   * @date : 2025-03-17 오후 10:29
   * @author : wongil
   * @Description: 지수 정보를 가져오기 위한 코드 현재 날짜로부터 -1일까지의 데이터만 긁어옴
   **/
  private String getAllInfosByCallOpenApi() {

    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
        .queryParam("serviceKey", API_KEY)
        .queryParam("resultType", "json")
        .queryParam("beginBasDt",
            convertToStringDateFormat(LocalDate.now().minusDays(WHAT_DAYS_FROM)))
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