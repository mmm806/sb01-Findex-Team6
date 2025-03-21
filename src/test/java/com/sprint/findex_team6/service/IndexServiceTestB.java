package com.sprint.findex_team6.service;

import com.sprint.findex_team6.dto.IndexInfoDto;
import com.sprint.findex_team6.dto.request.IndexInfoCreateRequest;
import com.sprint.findex_team6.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex_team6.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex_team6.entity.Index;
import com.sprint.findex_team6.entity.SourceType;
import com.sprint.findex_team6.repository.IndexRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IndexServiceTestB {

    private final IndexService indexService;
    private final IndexRepository indexRepository;

    @Autowired
    public IndexServiceTestB(IndexService indexService, IndexRepository indexRepository) {
        this.indexService = indexService;
        this.indexRepository = indexRepository;
    }

    private Index index;

    @BeforeEach
    void setUp() {
        // Given: 기존 데이터 초기화
        indexRepository.deleteAll(); // 이전 데이터 삭제
        index = new Index("KOSPI", "IT 서비스", 200, LocalDate.now(), BigDecimal.valueOf(1000), SourceType.USER, true);
        indexRepository.save(index); // 초기 데이터 삽입
    }

    @AfterEach
    void tearDown() {
        // AfterEach: 테스트 후 데이터베이스 정리
        indexRepository.deleteAll();
    }

    @Test
    void create() {
        IndexInfoCreateRequest request = new IndexInfoCreateRequest("aa", "name", 20, LocalDate.now(), BigDecimal.ONE, true);
        indexService.create(request);
    }

    @Test
    void update() {
        // Given: 기존에 저장된 지수 정보
        IndexInfoUpdateRequest updateRequest = new IndexInfoUpdateRequest(300, LocalDate.now(), BigDecimal.TEN, false);

        // When: update 메서드를 실행
        indexService.update(updateRequest, index.getId());

        // Then: 지수 정보가 업데이트되었는지 확인
        Index updatedIndex = indexRepository.findById(index.getId()).orElse(null);
        assertNotNull(updatedIndex);  // 지수 정보가 존재해야 함
        assertEquals(300, updatedIndex.getEmployedItemsCount());  // 수정된 종목 수 확인
        assertEquals(BigDecimal.TEN, updatedIndex.getBaseIndex());  // 수정된 기준 지수 확인
        assertFalse(updatedIndex.getFavorite());  // 수정된 즐겨찾기 여부 확인
    }

    @Test
    void delete() {
        indexService.delete(index.getId());
        assertFalse(indexRepository.existsById(index.getId()));  // 지수 정보가 삭제되었는지 확인
    }

    @Test
    void testGetIndexInfoById() {
        Long id = index.getId();
        IndexInfoDto result = indexService.getIndexInfoById(id);
        assertNotNull(result);
        assertEquals(index.getIndexName(), result.indexName());
    }


}
