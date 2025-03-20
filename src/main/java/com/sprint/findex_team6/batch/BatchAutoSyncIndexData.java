package com.sprint.findex_team6.batch;

import com.fasterxml.jackson.databind.JsonNode;
import com.sprint.findex_team6.entity.AutoIntegration;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.IndexVal;
import com.sprint.findex_team6.repository.AutoIntegrationRepository;
import com.sprint.findex_team6.repository.IndexDataLinkRepository;
import com.sprint.findex_team6.repository.IndexValRepository;
import com.sprint.findex_team6.service.util.SyncJobUtils;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchAutoSyncIndexData {

  private final AutoIntegrationRepository autoIntegrationRepository;
  private final IndexDataLinkRepository indexDataLinkRepository;
  private final IndexValRepository indexValRepository;

  private final RestTemplate restTemplate;

  @Value("${api.stock.url}")
  private String BASE_URL;

  @Value("${api.stock.key}")
  private String API_KEY;

  @Value("${index.data.batch.enable}")
  private boolean batchEnable;

  /**
   * @methodName : syncIndexData
   * @date : 2025-03-20 오후 4:09
   * @author : wongil
   * @Description: 배치를 이용해 주기적으로 지수 데이터 업데이트
   **/
  @Transactional
  @Scheduled(cron = "${index.data.batch.cron}")
  public void syncIndexData() {
    if (!batchEnable) {
      return;
    }

    List<Index> indexList = autoIntegrationRepository.findAllByEnabledIsTrue().stream()
        .map(AutoIntegration::getIndex)
        .toList();

    for (Index index : indexList) {
      try {
        sync(index);
      } catch (Exception e) {
        log.error("연동 실패: {}", index.getId(), e);
      }
    }
  }

  /**
  * @methodName : sync
  * @date : 2025-03-20 오후 4:26
  * @author : wongil
  * @Description: 실제 갱신 작업
  **/
  private void sync(Index index) {
    List<IndexVal> indexVals = indexValRepository.findByIndex_Id(index.getId());

    String response = getAllInfosByCallOpenApi();
    JsonNode items = getItems(response);

    for (JsonNode item : items) {
      indexVals
          .forEach(val -> {

            String indexName = item.path("idxNm").asText();
            String indexClassification = item.path("idxCsf").asText();

            if (!val.getIndex().getIndexName().equals(indexName) && !val.getIndex()
                .getIndexClassification().equals(indexClassification)) {

              return;
            }

            Double mkp = item.path("mkp").asDouble(); // 시가
            Double clpr = item.path("clpr").asDouble();// 종가
            Double hipr = item.path("hipr").asDouble();// 고가
            Double lopr = item.path("lopr").asDouble();// 저가
            Double vs = item.path("vs").asDouble();// 대비
            Double fltRt = item.path("fltRt").asDouble();// 등락률
            Long trqu = item.path("trqu").asLong();// 거래량
            Double trPrc = item.path("trPrc").asDouble();// 거래대금
            Double lstgMrktTotAmt = item.path("lstgMrktTotAmt").asDouble();// 상장시가총액
            String basDt = item.path("basDt").asText();// 가져온 데이터 날짜

            val.changeData(mkp, clpr, hipr, lopr, vs, fltRt, trqu, trPrc,
                lstgMrktTotAmt);
          });
    }
  }

  /**
   * @methodName : getItems
   * @date : 2025-03-20 오후 16:33
   * @author : wongil
   * @Description: api reponse body의 item 배열만 뽑기
   **/
  private JsonNode getItems(String response) {
    return SyncJobUtils.findItems(response)
        .path("response")
        .path("body")
        .path("items")
        .path("item");
  }

  /**
   * @methodName : getAllInfosByCallOpenApi
   * @date : 2025-03-20 오후 16:32
   * @author : wongil
   * @Description: 지수 정보를 가져오기 위한 코드 현재 날짜로부터 -1일까지의 데이터만 긁어옴
   **/
  private String getAllInfosByCallOpenApi() {

    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
        .queryParam("serviceKey", API_KEY)
        .queryParam("resultType", "json")
        .queryParam("beginBasDt",
            convertToStringDateFormat(LocalDate.now().minusDays(1)))
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 100);

    return getResponseBody(builder);
  }

  /**
   * @methodName : getResponseBody
   * @date : 2025-03-20 오후 16:32
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
   * @date : 2025-03-20 오후 16:32
   * @author : wongil
   * @Description: LocalDate을 "20240731" 이런 형식으로 변환
   **/
  private String convertToStringDateFormat(LocalDate localDate) {
    return String.join("", localDate.toString().split("-"));
  }
}
