package com.sysm.catalog.admin.infrastructure.aggregates.genre.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sysm.catalog.admin.infrastructure.aggregates.category.models.CategoryResponse;

import java.time.Instant;
import java.util.List;

public record GenreListResponse(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("categories") List<CategoryResponse> categories,
        @JsonProperty("is_active") Boolean active,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("deleted_at") Instant deletedAt
) {
}
