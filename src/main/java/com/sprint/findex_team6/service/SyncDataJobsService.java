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
public class SyncDataJobsService {

  private final IndexDataLinkRepository indexDataLinkRepository;
  private final IndexRepository indexRepository;

  @Value("${api.stock.url}")
  private String BASE_URL;

  @Value("${api.stock.key}")
  private String API_KEY;

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

    return getSyncJobDtoMono(webClient, request, indexListMono, httpRequest)
        .flatMapMany(Flux::fromIterable) // Mono<List> -> Flux 스트림으로 변환
        .flatMap(
            dto -> save(dto) // ItemDateLink -> repository에 저장
                .thenReturn(dto)
        );
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
            Comparator.comparing(SyncJobDto::id, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SyncJobDto::targetDate,
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
    Boolean result = isSuccessOrFalse(dto.result());

    return Mono.fromCallable(() -> {
          Index index = findIndex(dto.indexInfoId());

          if (dto.indexInfoId() == null || index == null) {
            return null;
          }

          IndexDataLink syncData = new IndexDataLink(
              null,
              dto.jobType(),
              dto.targetDate(),
              dto.worker(),
              dto.jobTime(),
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
  private Flux<SyncJobDto> convertToSyncJobDtoFlux(WebClient webClient, IndexDataSyncRequest request,
      Mono<List<Index>> indexListMono, HttpServletRequest httpRequest) {

    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .queryParam("serviceKey", API_KEY)
            .queryParam("resultType", "json")
            .queryParam("beginBasDt", convertToStringDateFormat(request.baseDateFrom()))
            .queryParam("endBasDt", convertToStringDateFormat(request.baseDateTo()))
            .queryParam("pageNo", 1) // 1페이지부터
            .queryParam("numOfRows", 100) // 기본적으로 데이터 100개씩 땡겨오기
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class) // json을 문자열로 바꿈
        .flatMapMany(response -> {

          JsonNode items = findItems(response);
          int numOfRows = getNumOfRows(items);
          int totalPages = getTotalPages(items);

          return indexListMono.flatMapMany(indexList -> {

            Flux<SyncJobDto> syncFlux = convertToSyncJobDtoFlux(httpRequest, indexList, items);

            return getNextPageSyncJobDtoFlux(webClient, request, httpRequest, indexList, totalPages,
                numOfRows,
                syncFlux);
          });
        });
  }

  /**
   * @methodName : getNextPageSyncJobDtoFlux
   * @date : 2025-03-15 오후 4:32
   * @author : wongil
   * @Description: 데이터의 양이 많아 페이지가 많으면 2페이지부터 totalPages - 1 번까지 반복해서 API 호출해서 데이터 가져오기
   **/
  private Flux<SyncJobDto> getNextPageSyncJobDtoFlux(WebClient webClient,
      IndexDataSyncRequest request,
      HttpServletRequest httpRequest, List<Index> indexList, int totalPages, int numOfRows,
      Flux<SyncJobDto> syncFlux) {
    if (totalPages > 1) {
      Flux<SyncJobDto> syncJobDtoFlux = Flux.range(2, totalPages - 1)
          .flatMap(pageNumber ->
              fetch(webClient, request, indexList, httpRequest, pageNumber, numOfRows));

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
  private Flux<SyncJobDto> convertToSyncJobDtoFlux(HttpServletRequest httpRequest, List<Index> indexList,
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
  private Flux<SyncJobDto> fetch(WebClient webClient, IndexDataSyncRequest request,
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

    return addSyncJob(items, indexList, nowLocalDateTime, httpRequest);
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
    Iterator<Index> iterator = indexList.iterator();

    for (JsonNode item : items) {
      LocalDate targetDate = getTargetDate(item);

      Index index = getIndex(iterator);
      if (index == null) {
        continue;
      }

      SyncJobDto dto = SyncJobDto.builder()
          .id(index.getId())
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

  /**
   * @methodName : getIndex
   * @date : 2025-03-15 오전 11:00
   * @author : wongil
   * @Description: index 반복적으로 가져오기
   **/
  private Index getIndex(Iterator<Index> iterator) {
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

    JsonNode jsonNode = null;

    try {
      jsonNode = objectMapper.readTree(response);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    /**
     * API 응답 구조
     "response": {
     "header": { ... },
     "body": {
     "items": {
     "item": [ ... ] }
     }
     }
     */
    return jsonNode
        .path("response")
        .path("body")
        .path("items")
        .path("item");
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
