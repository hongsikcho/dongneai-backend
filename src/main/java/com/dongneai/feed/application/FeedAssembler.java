package com.dongneai.feed.application;

import com.dongneai.feed.application.dto.Author;
import com.dongneai.feed.application.dto.PlaceTag;
import com.dongneai.feed.application.dto.PostSummaryResponse;
import com.dongneai.feed.domain.Post;
import com.dongneai.feed.domain.ReactionRepository;
import com.dongneai.member.api.MemberApi;
import com.dongneai.place.api.PlaceApi;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 글 목록 → 카드 변환. 작성자·장소·내 공감 여부를 한 번에 모아 읽는다. */
@Component
@RequiredArgsConstructor
class FeedAssembler {

    private final MemberApi memberApi;
    private final PlaceApi placeApi;
    private final ReactionRepository reactionRepository;

    Map<Long, String> nicknames(Collection<Long> userIds) {
        return memberApi.findNicknames(new HashSet<>(userIds));
    }

    Map<Long, PlaceApi.PlaceRef> places(Collection<Long> placeIds) {
        return placeApi.findPlaces(placeIds.stream().filter(Objects::nonNull).distinct().toList());
    }

    static PlaceTag tag(PlaceApi.PlaceRef ref) {
        return ref == null ? null : new PlaceTag(ref.id(), ref.name(), ref.category());
    }

    List<PostSummaryResponse> summaries(List<Post> posts, Long viewerId) {
        if (posts.isEmpty()) {
            return List.of();
        }
        Map<Long, String> nicknames = nicknames(posts.stream().map(Post::getUserId).toList());
        Map<Long, PlaceApi.PlaceRef> places = places(posts.stream().map(Post::getPlaceId).toList());
        Set<Long> liked = new HashSet<>(reactionRepository.findLikedPostIds(viewerId,
                posts.stream().map(Post::getId).toList()));
        return posts.stream()
                .map(p -> PostSummaryResponse.of(p, new Author(p.getUserId(), nicknames.get(p.getUserId())),
                        p.getPlaceId() == null ? null : tag(places.get(p.getPlaceId())), liked.contains(p.getId())))
                .toList();
    }
}
