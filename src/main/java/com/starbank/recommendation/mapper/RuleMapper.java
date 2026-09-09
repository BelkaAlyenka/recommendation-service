package com.starbank.recommendation.mapper;

import com.starbank.recommendation.domain.QueryEntity;
import com.starbank.recommendation.domain.RuleEntity;
import com.starbank.recommendation.dto.QueryDto;
import com.starbank.recommendation.dto.RuleDto;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RuleMapper {

    public RuleEntity toEntity(RuleDto dto) {
        RuleEntity ruleEntity = new RuleEntity();
        ruleEntity.setId(dto.id() != null ? dto.id() : UUID.randomUUID());
        ruleEntity.setProductName(dto.productName());
        ruleEntity.setProductId(dto.productId());
        ruleEntity.setProductText(dto.productText());

        if (dto.rule() != null) {
            dto.rule().stream()
                    .map(this::toQueryEntity)
                    .forEach(ruleEntity::addQuery);
        }
        return ruleEntity;
    }

    public RuleDto toDto(RuleEntity entity) {
        var queryDtos = entity.getQueries().stream()
                .map(this::toQueryDto)
                .collect(Collectors.toList());

        return new RuleDto(
                entity.getId(),
                entity.getProductName(),
                entity.getProductId(),
                entity.getProductText(),
                queryDtos
        );
    }

    private QueryEntity toQueryEntity(QueryDto dto) {
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setQuery(dto.query());
        queryEntity.setNegate(dto.negate());
        queryEntity.setArguments(dto.arguments());
        return queryEntity;
    }

    private QueryDto toQueryDto(QueryEntity entity) {
        return new QueryDto(
                entity.getQuery(),
                entity.getArguments(),
                entity.isNegate()
        );
    }
}
