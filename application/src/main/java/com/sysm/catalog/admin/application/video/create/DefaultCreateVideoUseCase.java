package com.sysm.catalog.admin.application.video.create;

import com.sysm.catalog.admin.domain.Identifier;
import com.sysm.catalog.admin.domain.aggregates.castmember.CastMemberGateway;
import com.sysm.catalog.admin.domain.aggregates.castmember.CastMemberID;
import com.sysm.catalog.admin.domain.aggregates.category.CategoryGateway;
import com.sysm.catalog.admin.domain.aggregates.category.CategoryID;
import com.sysm.catalog.admin.domain.aggregates.video.MediaResourceGateway;
import com.sysm.catalog.admin.domain.aggregates.video.Video;
import com.sysm.catalog.admin.domain.aggregates.video.VideoGateway;
import com.sysm.catalog.admin.domain.aggregates.video.VideoResource;
import com.sysm.catalog.admin.domain.exceptions.InternalErrorException;
import com.sysm.catalog.admin.domain.exceptions.NotificationException;
import com.sysm.catalog.admin.domain.aggregates.genre.GenreGateway;
import com.sysm.catalog.admin.domain.aggregates.genre.GenreID;
import com.sysm.catalog.admin.domain.validation.ValidationHandler;
import com.sysm.catalog.admin.domain.validation.handler.Notification;
import com.sysm.catalog.admin.domain.aggregates.video.enums.Rating;
import com.sysm.catalog.admin.domain.aggregates.video.enums.VideoMediaType;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultCreateVideoUseCase extends CreateVideoUseCase {

    private final CategoryGateway categoryGateway;
    private final CastMemberGateway castMemberGateway;
    private final GenreGateway genreGateway;
    private final MediaResourceGateway mediaResourceGateway;
    private final VideoGateway videoGateway;

    public DefaultCreateVideoUseCase(
            final CategoryGateway categoryGateway,
            final CastMemberGateway castMemberGateway,
            final GenreGateway genreGateway,
            final MediaResourceGateway mediaResourceGateway,
            final VideoGateway videoGateway
    ) {
        this.categoryGateway = Objects.requireNonNull(categoryGateway);
        this.castMemberGateway = Objects.requireNonNull(castMemberGateway);
        this.genreGateway = Objects.requireNonNull(genreGateway);
        this.mediaResourceGateway = Objects.requireNonNull(mediaResourceGateway);
        this.videoGateway = Objects.requireNonNull(videoGateway);
    }

    @Override
    public CreateVideoOutput execute(final CreateVideoCommand aCommand) {
        final var aRating = Rating.of(aCommand.rating()).orElse(null);
        final var aLaunchYear = aCommand.launchedAt() != null ? Year.of(aCommand.launchedAt()) : null;
        final var categories = toIdentifier(aCommand.categories(), CategoryID::from);
        final var genres = toIdentifier(aCommand.genres(), GenreID::from);
        final var members = toIdentifier(aCommand.members(), CastMemberID::from);

        final var notification = Notification.create();
        notification.append(validateCategories(categories));
        notification.append(validateGenres(genres));
        notification.append(validateMembers(members));

        final var aVideo = Video.newVideo(
                aCommand.title(),
                aCommand.description(),
                aLaunchYear,
                aCommand.duration(),
                aCommand.opened(),
                aCommand.published(),
                aRating,
                categories,
                genres,
                members
        );

        aVideo.validate(notification);

        if (notification.hasError()) {
            throw new NotificationException("Could not create Aggregate Video", notification);
        }

        return CreateVideoOutput.from(create(aCommand, aVideo));
    }

    private Video create(final CreateVideoCommand aCommand, final Video aVideo) {
        final var anId = aVideo.getId();

        try {
            final var aVideoMedia = aCommand.getVideo()
                    .map(it -> this.mediaResourceGateway.storeAudioVideo(anId, VideoResource.with(it, VideoMediaType.VIDEO)))
                    .orElse(null);

            final var aTrailerMedia = aCommand.getTrailer()
                    .map(it -> this.mediaResourceGateway.storeAudioVideo(anId, VideoResource.with(it, VideoMediaType.TRAILER)))
                    .orElse(null);

            final var aBannerMedia = aCommand.getBanner()
                    .map(it -> this.mediaResourceGateway.storeImage(anId, VideoResource.with(it, VideoMediaType.BANNER)))
                    .orElse(null);

            final var aThumbnailMedia = aCommand.getThumbnail()
                    .map(it -> this.mediaResourceGateway.storeImage(anId, VideoResource.with(it, VideoMediaType.THUMBNAIL)))
                    .orElse(null);

            final var aThumbHalfMedia = aCommand.getThumbnailHalf()
                    .map(it -> this.mediaResourceGateway.storeImage(anId, VideoResource.with(it, VideoMediaType.THUMBNAIL_HALF)))
                    .orElse(null);

            return this.videoGateway.create(
                    aVideo
                            .updateVideoMedia(aVideoMedia)
                            .updateTrailerMedia(aTrailerMedia)
                            .updateBannerMedia(aBannerMedia)
                            .updateThumbnailMedia(aThumbnailMedia)
                            .updateThumbnailHalfMedia(aThumbHalfMedia)
            );
        } catch (final Throwable t) {
            this.mediaResourceGateway.clearResources(anId);
            throw InternalErrorException.with(
                    "An error on create video was observed [videoId:%s]".formatted(anId.getValue()),
                    t
            );
        }
    }

    private ValidationHandler validateCategories(final Set<CategoryID> ids) {
        return validateAggregate("categories", ids, categoryGateway::existsByIds);
    }

    private ValidationHandler validateGenres(final Set<GenreID> ids) {
        return validateAggregate("genres", ids, genreGateway::existsByIds);
    }

    private ValidationHandler validateMembers(final Set<CastMemberID> ids) {
        return validateAggregate("cast members", ids, castMemberGateway::existsByIds);
    }

    private <T extends Identifier> ValidationHandler validateAggregate(
            final String aggregate,
            final Set<T> ids,
            final Function<Iterable<T>, List<T>> existsByIds
    ) {
        final var notification = Notification.create();
        if (ids == null || ids.isEmpty()) {
            return notification;
        }

        final var retrievedIds = existsByIds.apply(ids);

        if (ids.size() != retrievedIds.size()) {
            final var missingIds = new ArrayList<>(ids);
            missingIds.removeAll(retrievedIds);

            final var missingIdsMessage = missingIds.stream()
                    .map(Identifier::getValue)
                    .collect(Collectors.joining(", "));

            notification.append(new Error("Some %s could not be found: %s".formatted(aggregate, missingIdsMessage)));
        }

        return notification;
    }

    private <T> Set<T> toIdentifier(final Set<String> ids, final Function<String, T> mapper) {
        return ids.stream()
                .map(mapper)
                .collect(Collectors.toSet());
    }
}