package com.starbank.recommendation.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "rule_stats")
public class RuleStatsEntity {

    @Id
    @Column(name = "rule_id")
    private UUID ruleId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;

    @Column(name = "count", nullable = false)
    private long count = 0;

    public RuleStatsEntity() {
    }

    public RuleStatsEntity(RuleEntity rule, long count) {
        this.rule = rule;
        this.ruleId = rule.getId();
        this.count = count;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    public RuleEntity getRule() {
        return rule;
    }

    public void setRule(RuleEntity rule) {
        this.rule = rule;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}