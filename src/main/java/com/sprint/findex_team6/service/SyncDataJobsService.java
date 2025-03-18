package com.sprint.findex_team6.service;

import static com.sprint.findex_team6.service.util.SyncJobUtils.getUserIp;

import com.fasterxml.jackson.databind.JsonNode;
import com.sprint.findex_team6.dto.SyncJobDto;
import com.sprint.findex_team6.dto.request.IndexDataSyncRequest;
import com.sprint.findex_team6.entity.ContentType;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexDataLink;
import com.sprint.findex_team6.repository.IndexDataLinkRepository;
import com.sprint.findex_team6.repository.IndexRepository;
import com.sprint.findex_team6.repository.IndexValRepository;
import com.sprint.findex_team6.service.util.SyncJobUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
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

    // 우선 지수 정보를 가져와야함
    List<Index> indexList = findAllIndexList(request.indexInfoIds());

    // 지수 정보가 있어야 지수 데이터 연동 가능
    if (indexList == null || indexList.isEmpty()) {
      throw new RuntimeException("지수 정보가 없습니다.");
    }

    List<String> indexNames = indexList.stream()
        .map(Index::getIndexName)
        .toList();

    // idxNm(index_name)와 일치하는 데이터 가져와야함
    List<JsonNode> jsonNodeList = getJsonNodeList(request, indexNames);
    int syncDataCount = getSyncDataCount(jsonNodeList);

    List<SyncJobDto> dtos = createMockSynDataJob(syncDataCount, httpRequest, syncJobDtoList);

    List<IndexDataLink> indexDataLinks = saveMockDtoToIndexDataLink(dtos, indexList, httpRequest);

    return dtos;
    

//    // 지수 정보를 바탕으로 DTO 생성
//    List<SyncJobDto> dtos = SyncJobUtils.createMockSyncJobResponse(items, httpRequest,
//        syncJobDtoList,
//        ContentType.INDEX_DATA);
//
//    // 만든 dto가 아직 id가 지정되지 않았음. 이걸 따로 지정해줘야함
//    List<IndexDataLink> indexDataLinks = saveMockDtoToIndexDataLink(dtos, indexList, httpRequest);
//
//    return dtos;
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
  * @methodName : getSyncDataCount
  * @date : 2025-03-18 오후 7:56
  * @author : wongil
  * @Description: 연동되어야 하는 데이터의 개수 얻기
  **/
  private int getSyncDataCount(List<JsonNode> jsonNodeList) {
    int count = 0;
    for (JsonNode jsonNode : jsonNodeList) {
      count += SyncJobUtils.getTotalCount(jsonNode);
    }

    return count;
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
        .flatMap(items -> {
          if (items.isArray()) {
            List<JsonNode> itemList = new ArrayList<>();
            items.forEach(itemList::add);

            return itemList.stream();
          }

          return Stream.of(items);
        })
        .toList();
  }

  /**
  * @methodName : getJsonNodeStream
  * @date : 2025-03-18 오후 7:22
  * @author : wongil
  * @Description: indexNames로 쿼리해서 JsonNode 얻기
  **/
  private Stream<JsonNode> getJsonNodeStream(List<String> indexNames, IndexDataSyncRequest request) {
    return indexNames.stream()
        .map(indexName -> {
          String response;

          if (request.indexInfoIds() == null || request.indexInfoIds().isEmpty()) {
            response = getInfoByBetweenDateOpenApi(request, indexName); // 모든 Index에 대해서 쿼리함
          } else {
            response = getInfoByClassificationOpenApi(request, indexName); // 특정 id로 조회된 것만 쿼리
          }

          return SyncJobUtils.findItems(response);
        });
  }

  /**
   * @methodName : getInfoByClassificationOpenApi
   * @date : 2025-03-18 오후 6:24
   * @author : wongil
   * @Description: 특정 id로 조회한 index를 찾아, classfication, index name으로 데이터를 찾아 연동해줘야함
   **/
  private String getInfoByClassificationOpenApi(IndexDataSyncRequest request, String indexName) {

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
   * @methodName : saveMockDtoToIndexDataLink
   * @date : 2025-03-18 오후 6:21
   * @author : wongil
   * @Description: dto와 index로 indexDataLink 객체 생성
   **/
  private List<IndexDataLink> saveMockDtoToIndexDataLink(List<SyncJobDto> dtos,
      List<Index> indexList, HttpServletRequest request) {

    List<IndexDataLink> links = new ArrayList<>();

    for (int i = 0; i < dtos.size() && i < indexList.size(); i++) {
      SyncJobDto dto = dtos.get(i);
      Index index = indexList.get(i);

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
    }

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
   * @Description: 사용자가 보낸 List<Integer> indexInfoIds 이걸 받아서 index 찾기
   **/
  private List<Index> findAllIndexList(List<Integer> indexInfoIds) {
    if (indexInfoIds == null || indexInfoIds.isEmpty()) {
      return indexRepository.findAll();
    }

    return indexRepository.findAllByIdIn(indexInfoIds);
  }
}
