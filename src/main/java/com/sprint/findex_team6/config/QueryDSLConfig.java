package com.sprint.findex_team6.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @package : com.sprint.findex_team6.config
 * @name : QueryDslConfig.java
 * @date : 2025-03-17 오후 2:07
 * @author : wongil
 * @Description: JPAQueryFactory 생성자로 주입 받기 코드 일일히 작성하기 싫어서 Bean에 등록
 **/
@Configuration
public class QueryDSLConfig {

    @Bean
    JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }
}
