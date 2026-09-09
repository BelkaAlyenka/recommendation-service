package com.starbank.recommendation.cache.key;

import java.util.UUID;

public record UserProductKey(UUID userId, String productType) {
}
