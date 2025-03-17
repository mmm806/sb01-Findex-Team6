package com.sprint.findex_team6.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.IndexDataSyncRequest;
import com.sprint.findex_team6.entity.ContentType;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexDataLink;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.repository.IndexDataLinkRepository;
import com.sprint.findex_team6.repository.IndexRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SyncJobsService {

  private final IndexDataLinkRepository indexDataLinkRepository;
  private final IndexRepository indexRepository;

  @Value("${api.stock.url}")
  private String BASE_URL;

  @Value("${api.stock.key}")
  private String API_KEY;

  /**
   * @methodName : syncInfo
   * @date : 2025-03-15 오후 10:16
   * @author : wongil
   * @Description: 지수 정보 연동
   **/
  public Flux<SyncJobDto> syncInfo() {
    List<SyncJobDto> syncIndexInfoJobDtoList = new ArrayList<>();
    WebClient webClient = getWebClient();

    return getSyncInfoJobDtoFlux(webClient, syncIndexInfoJobDtoList);
  }

  /**
   * @methodName : syncData
   * @date : 2025-03-14 오후 1:00
   * @author : wongil
   * @Description: 지수 데이터 연동 지수, 대상 날짜로 연동할 데이터 범위 지정할 수 있음 대상 날짜는 필수 지수는 선택적 대상 지수, 대상 날짜가 여러 개인 경우
   * 지수, 날짜 별로 이력을 등록
   **/
  public Flux<SyncJobDto> syncData(IndexDataSyncRequest request, HttpServletRequest httpRequest) {

    WebClient webClient = getWebClient();

    Mono<List<Index>> indexListMono = findIndexListMonoByIds(request.indexInfoIds());

    return getSyncDataJobDtoFlux(request, httpRequest, webClient, indexListMono);
  }

  /**
  * @methodName : getSyncDataJobDtoFlux
  * @date : 2025-03-17 오전 9:35
  * @author : wongil
  * @Description: Index Data Link 엔티티를 실제로 저장
   * Mono<List<SyncJobDto> -> Flux
  **/
  private Flux<SyncJobDto> getSyncDataJobDtoFlux(IndexDataSyncRequest request,
      HttpServletRequest httpRequest, WebClient webClient, Mono<List<Index>> indexListMono) {

    return getSyncJobDtoMono(webClient, request, indexListMono, httpRequest)
        .flatMapMany(Flux::fromIterable) // Mono<List> -> Flux 스트림으로 변환
        .flatMap(
            dto ->
                save(dto) // ItemDateLink -> repository에 저장
                    .then(Mono.fromCallable(() -> getSavedIndexDataLinkId(dto))
                        .subscribeOn(Schedulers.boundedElastic())
                        .map(id -> {
                          dto.setId(id);
                          return dto;
                        })
                    ));
  }

  /**
   * @methodName : getSyncJobDtoFlux
   * @date : 2025-03-16 오후 10:10
   * @author : wongil
   * @Description: 지수 정보를 실제로 저장하고 Flux로 반환
   **/
  private Flux<SyncJobDto> getSyncInfoJobDtoFlux(WebClient webClient,
      List<SyncJobDto> syncIndexInfoJobDtoList) {

    return getAllInfosByCallOpenApi(webClient)
        .flatMapMany(response -> {
          JsonNode items = getItems(response);
          List<SyncJobDto> first = saveIndex(syncIndexInfoJobDtoList, items);

          JsonNode countItems = findItems(response);
          int numOfRows = getNumOfRows(countItems);
          int totalPages = getTotalPages(countItems);

          Flux<SyncJobDto> syncJobDtoFlux = Flux.fromIterable(syncIndexInfoJobDtoList);
          return getNextPageSyncInfoJobDtoFlux(syncJobDtoFlux, webClient, syncIndexInfoJobDtoList, numOfRows, totalPages);

        })
        .onErrorResume(e -> {
          log.error("Sync job error: {}", e.getMessage(), e);
          return Flux.empty();
        });
  }

  /**
  * @methodName : getItems
  * @date : 2025-03-17 오전 9:56
  * @author : wongil
  * @Description: JsonNode에서 item 배열만 가져오기
  **/
  private JsonNode getItems(String response) {
    JsonNode itemNodes = findItems(response);

    return parsingData(itemNodes);
  }

  /**
  * @methodName : saveIndex
  * @date : 2025-03-17 오전 9:54
  * @author : wongil
  * @Description: items가 list인 경우 파싱해서 저장
  **/
  private List<SyncJobDto> saveIndex(List<SyncJobDto> syncIndexInfoJobDtoList, JsonNode items) {
    if (items.isArray()) {
      saveIndexInfo(items, syncIndexInfoJobDtoList);
    }

    return syncIndexInfoJobDtoList;
  }

  /**
  * @methodName : getNextPageSyncInfoJobDtoFlux
  * @date : 2025-03-17 오전 9:41
  * @author : wongil
  * @Description: 페이지의 수가 2개 이상인 경우 계속 돌면서 데이터 가져오기
  **/
  private Flux<SyncJobDto> getNextPageSyncInfoJobDtoFlux(Flux<SyncJobDto> syncJobDtoFlux, WebClient webClient, List<SyncJobDto> syncIndexInfoJobDtoList, int numOfRows,
      int totalPages) {

    if(totalPages > 1) {
      log.info("Fetching additional pages. Total pages: {}", totalPages);

      log.info("Will fetch from page 2 to page {}", totalPages);

      Flux<SyncJobDto> syncJobIndexInfoDtoFlux = Flux.range(2, totalPages - 1)
          .doOnNext(pageNumber -> log.info("Processing page number: {}", pageNumber))
          .flatMap(pageNumber ->
              fetchInfo(webClient, new ArrayList<>(), pageNumber, numOfRows)
                  .doOnComplete(() -> log.info("Completed fetching page: {}", pageNumber)));

      return syncJobDtoFlux.concatWith(syncJobIndexInfoDtoFlux);
    }

    return syncJobDtoFlux;
  }

  private Flux<SyncJobDto> fetchInfo(WebClient webClient, List<SyncJobDto> syncIndexInfoJobDtoList,
      Integer pageNumber, int numOfRows) {

    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .queryParam("serviceKey", API_KEY)
            .queryParam("resultType", "json")
            .queryParam("beginBasDt", convertToStringDateFormat(LocalDate.now().minusDays(3)))
            .queryParam("endBasDt", convertToStringDateFormat(LocalDate.now()))
            .queryParam("pageNo", pageNumber)
            .queryParam("numOfRows", numOfRows)
            .build()
        )
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class)
        .flatMapMany(response -> {
          JsonNode items = getItems(response);

          List<SyncJobDto> savedSyncIndexInfoJobDtoList = saveIndex(syncIndexInfoJobDtoList, items);

          return Flux.fromIterable(savedSyncIndexInfoJobDtoList);
        });

  }

  /**
   * @methodName : saveIndexInfo
   * @date : 2025-03-16 오후 10:07
   * @author : wongil
   * @Description: 파싱된 item 가지고 실제 Index 객체 만들어서 db에 저장
   **/
  private void saveIndexInfo(JsonNode items, List<SyncJobDto> syncIndexInfoJobDtoList) {
    for (JsonNode item : items) {

      Index index = createIndex(item);

      Index savedIndex = indexRepository.save(index);

      createSynJobDtoList(savedIndex, syncIndexInfoJobDtoList);
    }
  }

  /**
  * @methodName : parsingData
  * @date : 2025-03-16 오후 10:06
  * @author : wongil
  * @Description: api 응답 json에서 파싱
  **/
  private JsonNode parsingData(JsonNode item) {
    return item
        .path("response")
        .path("body")
        .path("items")
        .path("item");
  }

  /**
   * @methodName : createSynJobDtoList
   * @date : 2025-03-16 오후 10:05
   * @author : wongil
   * @Description: dto flux를 위해 리스트에 추가
   **/
  private void createSynJobDtoList(Index savedIndex, List<SyncJobDto> syncIndexInfoJobDtoList) {
    SyncJobDto dto = SyncJobDto.builder()
        .id(savedIndex.getId())
        .jobType(ContentType.INDEX_INFO)
        .jobTime(LocalDateTime.now())
        .targetDate(savedIndex.getBaseDate())
        .indexInfoId(savedIndex.getId())
        .worker("system")
        .build();

    syncIndexInfoJobDtoList.add(dto);
  }

  /**
   * @methodName : createIndex
   * @date : 2025-03-16 오후 9:17
   * @author : wongil
   * @Description: 파싱된 데이터 가지고 Index 객체 생성
   **/
  private Index createIndex(JsonNode parsedData) {
    String indexClassification = parsedData.path("idxCsf").asText();
    String idxNm = parsedData.path("idxNm").asText();
    int employedItemsCount = parsedData.path("epyItmsCnt").asInt();
    String baseDate = parsedData.path("basPntm").asText();
    String baseIndex = parsedData.path("basIdx").asText();
    SourceType sourceType = SourceType.OPEN_API;
    Boolean favorite = false;

    return new Index(
        indexClassification,
        idxNm,
        employedItemsCount,
        LocalDate.parse(baseDate, DateTimeFormatter.ofPattern("yyyMMdd")),
        new BigDecimal(baseIndex),
        sourceType, favorite);
  }

  /**
   * @methodName : getCallOpenApi
   * @date : 2025-03-16 오후 2:04
   * @author : wongil
   * @Description: 지수 정보를 가져오기 위한 코드 현재 날짜로부터 -3일까지의 데이터만 긁어옴
   **/
  private Mono<String> getAllInfosByCallOpenApi(WebClient webClient) {

    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .queryParam("serviceKey", API_KEY)
            .queryParam("resultType", "json")
            .queryParam("beginBasDt", convertToStringDateFormat(LocalDate.now().minusDays(4))) // 일단 3일로
            .queryParam("pageNo", 1)
            .queryParam("numOfRows", 100)
            .build()
        )
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class);
  }

  /**
   * @methodName : getSavedIndexDataLinkId
   * @date : 2025-03-16 오후 2:43
   * @author : wongil
   * @Description: 저장된 IndexDataLink의 id값 가져오기
   **/
  private Long getSavedIndexDataLinkId(SyncJobDto dto) {

    IndexDataLink savedIndexDataLink = indexDataLinkRepository.findByIndex_IdAndAndTargetDateAndJobTime(
        dto.getIndexInfoId(),
        dto.getTargetDate(), dto.getJobTime());

    return savedIndexDataLink.getId();
  }

  /**
   * @methodName : findIndexListMonoByIds
   * @date : 2025-03-15 오전 10:45
   * @author : wongil
   * @Description: IndexRepository에서 Index.id로 Index 뽑기
   **/
  private Mono<List<Index>> findIndexListMonoByIds(List<Integer> indexInfoIds) {

    return Mono.fromCallable(() ->
            indexRepository.findAllByIdIn(indexInfoIds))
        .subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * @methodName : getMonos
   * @date : 2025-03-14 오후 9:27
   * @author : wongil
   * @Description: Flux 스트림을 지수, 날짜 별로 정렬하고 Mono로 반환
   **/
  private Mono<List<SyncJobDto>> getSyncJobDtoMono(WebClient webClient,
      IndexDataSyncRequest request, Mono<List<Index>> indexListMono,
      HttpServletRequest httpRequest) {

    return convertToSyncJobDtoFlux(webClient, request, indexListMono, httpRequest)
        .collectSortedList( // 1. 지수, 2. 날짜 별로 정렬
            Comparator.comparing(SyncJobDto::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SyncJobDto::getTargetDate,
                    Comparator.nullsLast(Comparator.naturalOrder()))
        );
  }

  /**
   * @methodName : save
   * @date : 2025-03-14 오후 5:53
   * @author : wongil
   * @Description: Flux 안에 있는 SyncJobDto를 각각 실제 repository에 저장
   **/
  private Mono<Void> save(SyncJobDto dto) {
    Boolean result = isSuccessOrFalse(dto.getResult());

    return Mono.fromCallable(() -> {
          Index index = findIndex(dto.getIndexInfoId());

          if (dto.getIndexInfoId() == null || index == null) {
            return null;
          }

          IndexDataLink syncData = new IndexDataLink(
              dto.getId(),
              dto.getJobType(),
              dto.getTargetDate(),
              dto.getWorker(),
              dto.getJobTime(),
              result,
              index);

          return indexDataLinkRepository.save(syncData);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .filter(Objects::nonNull)  // null 결과는 필터링
        .onErrorResume(e -> {
          log.error("Error in save operation: {}", e.getMessage(), e);
          return Mono.empty();
        })
        .then();
  }

  /**
   * @methodName : isSuccessOrFalse
   * @date : 2025-03-14 오후 9:46
   * @author : wongil
   * @Description: "SUCCESS"면 true, "FAILED"면 false
   **/
  private Boolean isSuccessOrFalse(String result) {
    return !result.equals("FAILED");
  }

  /**
   * @methodName : findIndex
   * @date : 2025-03-15 오후 2:23
   * @author : wongil
   * @Description: indexRepository에서 지수 꺼내오기
   **/
  private Index findIndex(Long indexId) {
    return indexRepository.findById(indexId)
        .orElse(null);
  }

  /**
   * @methodName : getSyncJobDtoFlux
   * @date : 2025-03-14 오후 4:49
   * @author : wongil
   * @Description: 외부 API를 호출하고 SyncJobDto 반환
   **/
  private Flux<SyncJobDto> convertToSyncJobDtoFlux(WebClient webClient,
      IndexDataSyncRequest request,
      Mono<List<Index>> indexListMono, HttpServletRequest httpRequest) {

    return getBetweenDateInfoByOpenApi(webClient, request) // json을 문자열로 바꿈
        .flatMapMany(response -> {

          JsonNode items = findItems(response);
          int numOfRows = getNumOfRows(items);
          int totalPages = getTotalPages(items);

          return indexListMono.flatMapMany(indexList -> {

            Flux<SyncJobDto> syncFlux = convertToSyncJobDtoFlux(httpRequest, indexList, items);

            return getNextPageSyncDataJobDtoFlux(webClient, request, httpRequest, indexList, totalPages,
                numOfRows,
                syncFlux);
          });
        });
  }

  /**
   * @methodName : getBetweenInfoByOpenApi
   * @date : 2025-03-16 오후 2:48
   * @author : wongil
   * @Description: beginBasDt와 endBasDt 사이의 데이터 뽑기
   **/
  private Mono<String> getBetweenDateInfoByOpenApi(WebClient webClient, IndexDataSyncRequest request) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .queryParam("serviceKey", API_KEY)
            .queryParam("resultType", "json")
            .queryParam("beginBasDt", convertToStringDateFormat(request.baseDateFrom()))
            .queryParam("endBasDt", convertToStringDateFormat(request.baseDateTo()))
            .queryParam("pageNo", 1)
            .queryParam("numOfRows", 100)
            .build()
        )
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class);
  }

  /**
   * @methodName : getNextPageSyncJobDtoFlux
   * @date : 2025-03-15 오후 4:32
   * @author : wongil
   * @Description: 데이터의 양이 많아 페이지가 많으면 2페이지부터 totalPages - 1 번까지 반복해서 API 호출해서 데이터 가져오기
   **/
  private Flux<SyncJobDto> getNextPageSyncDataJobDtoFlux(WebClient webClient,
      IndexDataSyncRequest request,
      HttpServletRequest httpRequest, List<Index> indexList, int totalPages, int numOfRows,
      Flux<SyncJobDto> syncFlux) {

    if (totalPages > 1) {
      Flux<SyncJobDto> syncJobDtoFlux = Flux.range(2, totalPages - 1)
          .flatMap(pageNumber ->
              fetchData(webClient, request, indexList, httpRequest, pageNumber, numOfRows));

      return syncFlux.concatWith(syncJobDtoFlux);
    }

    return syncFlux;
  }

  /**
   * @methodName : getSyncJobDtoFlux
   * @date : 2025-03-15 오후 4:34
   * @author : wongil
   * @Description: List<SyncJobDto> -> Flux로 변환
   **/
  private Flux<SyncJobDto> convertToSyncJobDtoFlux(HttpServletRequest httpRequest,
      List<Index> indexList,
      JsonNode items) {

    List<SyncJobDto> syncJobDtoList = getSyncJobDtoList(items, indexList, httpRequest);

    return Flux.fromIterable(syncJobDtoList);
  }

  /**
   * @methodName : fetch
   * @date : 2025-03-15 오후 4:35
   * @author : wongil
   * @Description: 나머지 pageNumber와 numberOfRows에 대하여 API 호출해서 데이터 뽑기
   **/
  private Flux<SyncJobDto> fetchData(WebClient webClient, IndexDataSyncRequest request,
      List<Index> indexList, HttpServletRequest httpRequest, int pageNumber, int numOfRows) {

    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .queryParam("serviceKey", API_KEY)
            .queryParam("resultType", "json")
            .queryParam("beginBasDt", convertToStringDateFormat(request.baseDateFrom()))
            .queryParam("endBasDt", convertToStringDateFormat(request.baseDateTo()))
            .queryParam("pageNo", pageNumber)
            .queryParam("numOfRows", numOfRows)
            .build()
        )
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class)
        .flatMapMany(response -> {
          JsonNode items = findItems(response);

          List<SyncJobDto> syncJobDtoList = getSyncJobDtoList(items, indexList, httpRequest);
          return Flux.fromIterable(syncJobDtoList);
        });
  }

  /**
   * @methodName : getTotalPages
   * @date : 2025-03-15 오후 4:36
   * @author : wongil
   * @Description: 총 페이지 수 구하기
   **/
  private int getTotalPages(JsonNode items) {
    int numOfRows = getNumOfRows(items);
    int totalCount = getTotalCount(items);

    return (int) Math.ceil((double) totalCount / numOfRows);
  }

  /**
   * @methodName : getTotalCount
   * @date : 2025-03-15 오후 4:36
   * @author : wongil
   * @Description: API 응답 바디에서 totalCount만 뽑기
   **/
  private int getTotalCount(JsonNode items) {
    return items.path("response")
        .path("body")
        .path("totalCount")
        .asInt();
  }

  /**
   * @methodName : getNumOfRows
   * @date : 2025-03-15 오후 4:36
   * @author : wongil
   * @Description: API 응답 바디에서 numOfRows 뽑기
   **/
  private int getNumOfRows(JsonNode items) {
    return items.path("response")
        .path("body")
        .path("numOfRows")
        .asInt();
  }

  /**
   * @methodName : convertToStringDateFormat
   * @date : 2025-03-14 오후 4:38
   * @author : wongil
   * @Description: LocalDate을 "20240731" 이런 형식으로 변환
   **/
  private String convertToStringDateFormat(LocalDate localDate) {
    return String.join("", localDate.toString().split("-"));
  }

  /**
   * @methodName : getSyncJobDtos
   * @date : 2025-03-14 오후 4:23
   * @author : wongil
   * @Description: SyncJobDto의 List로 변경
   **/
  private List<SyncJobDto> getSyncJobDtoList(JsonNode items, List<Index> indexList,
      HttpServletRequest httpRequest) {

    LocalDateTime nowLocalDateTime = LocalDateTime.now();

    return addSyncJob(items
        .path("response")
        .path("body")
        .path("items")
        .path("item"), indexList, nowLocalDateTime, httpRequest);
  }

  /**
   * @methodName : addSyncJob
   * @date : 2025-03-15 오전 11:12
   * @author : wongil
   * @Description: JsonNode Item -> SyncJobDto 변환 후 syncJobDtoList에 각각 추가
   **/
  private List<SyncJobDto> addSyncJob(JsonNode items, List<Index> indexList,
      LocalDateTime nowLocalDateTime, HttpServletRequest httpRequest) {

    List<SyncJobDto> syncJobDtoList = new ArrayList<>();

    for (JsonNode item : items) {
      LocalDate targetDate = getTargetDate(item);

      for (Index index : indexList) {
        SyncJobDto dto = SyncJobDto.builder()
            .id(null)
            .jobType(ContentType.INDEX_DATA)
            .indexInfoId(getIndexId(index))
            .targetDate(targetDate)
            .worker(getWorker(index, httpRequest))
            .jobTime(nowLocalDateTime)
            .result(isCompleteSync())
            .build();

        syncJobDtoList.add(dto);
      }

      return syncJobDtoList;
    }

    return syncJobDtoList;
  }

  /**
   * @methodName : getIndex
   * @date : 2025-03-15 오전 11:00
   * @author : wongil
   * @Description: index 반복적으로 가져오기
   **/
  private Index createIndex(Iterator<Index> iterator) {
    return iterator.hasNext() ? iterator.next() : null;
  }

  // TODO: 조건에 따라 연동이 되었는지 안되었는지 true, false 반환
  private String isCompleteSync() {
    return "SUCCESS"; // 임시
  }

  /**
   * @methodName : getWorker
   * @date : 2025-03-15 오전 11:16
   * @author : wongil
   * @Description: 지수 정보의 Source type이 USER면 사용자의 IP 주소 open_api와 같이 배치 작업으로 들어온 경우 system
   **/
  private String getWorker(Index index, HttpServletRequest httpRequest) {
    if (index != null && index.getSourceType() == SourceType.USER) {
      return getUserIp(httpRequest);
    } else if (index != null && index.getSourceType() == SourceType.OPEN_API) {
      return "system";
    }

    return getUserIp(httpRequest);
  }

  private String getUserIp(HttpServletRequest request) {
    return request.getRemoteAddr();
  }

  /**
   * @methodName : getIndexId
   * @date : 2025-03-15 오전 10:59
   * @author : wongil
   * @Description: Index.id 가져오기
   **/
  private Long getIndexId(Index index) {
    return index != null ? index.getId() : null;
  }

  /**
   * @methodName : getTargetDate
   * @date : 2025-03-14 오후 2:52
   * @author : wongil
   * @Description: open api의 basDt를 받아 LocalDateTime으로 변경
   **/
  private LocalDate getTargetDate(JsonNode item) {
    String dateStr = item.path("basDt").asText();

    dateStr = dateStr.replace("\"", ""); // api 응답 결과가 "20241203" 이렇게 나오는데 앞뒤 " <- 이거 제거해야함

    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyMMdd"));
  }

  /**
   * @methodName : findItems
   * @date : 2025-03-14 오후 5:52
   * @author : wongil
   * @Description: API 응답 결과 파싱해서 JsonNode 타입으로 반환
   **/
  private JsonNode findItems(String response) {
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      return objectMapper.readTree(response);
    } catch (JsonProcessingException e) {
      log.error("Raw API response: {}", response);
      throw new RuntimeException(e);
    }
  }

  /**
   * @methodName : getWebClient
   * @date : 2025-03-14 오후 1:34
   * @author : wongil
   * @Description: builderFactory를 통해서 실제 WebClient 뽑기
   **/
  private WebClient getWebClient() {
    DefaultUriBuilderFactory builderFactory = getNoneEncodingUriBuilder();

    return WebClient.builder()
        .baseUrl(BASE_URL)
        .uriBuilderFactory(builderFactory)
        .build();
  }

  /**
   * @methodName : setNoneEncodingUriBuilder
   * @date : 2025-03-14 오후 1:23
   * @author : wongil
   * @Description: UriBuilder는 기본적으로 파라미터 값을 인코딩 하게 되는데, api 키가 이미 인코딩 되어 있어서 중복 인코딩 되지 않게 막음
   **/
  private DefaultUriBuilderFactory getNoneEncodingUriBuilder() {
    DefaultUriBuilderFactory builderFactory = new DefaultUriBuilderFactory(BASE_URL);
    builderFactory.setEncodingMode(EncodingMode.VALUES_ONLY);

    return builderFactory;
  }

}
