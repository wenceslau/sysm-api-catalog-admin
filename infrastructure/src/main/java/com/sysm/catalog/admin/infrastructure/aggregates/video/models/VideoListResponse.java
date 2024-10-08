package com.sysm.catalog.admin.infrastructure.aggregates.video.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sysm.catalog.admin.infrastructure.aggregates.category.models.CategoryResponse;
import com.sysm.catalog.admin.infrastructure.aggregates.genre.models.GenreResponse;

import java.time.Instant;
import java.util.List;

public record VideoListResponse(
        @JsonProperty("id") String id,
        @JsonProperty("title") String title,
        @JsonProperty("genres") List<GenreResponse> genres,
        @JsonProperty("categories") List<CategoryResponse> categories,
        @JsonProperty("description") String description,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
