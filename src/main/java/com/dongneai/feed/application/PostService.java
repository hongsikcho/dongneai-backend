package com.dongneai.feed.application;

import com.dongneai.feed.application.dto.Author;
import com.dongneai.feed.application.dto.LikeResponse;
import com.dongneai.feed.application.dto.PostDetailResponse;
import com.dongneai.feed.application.dto.PostRequest;
import com.dongneai.feed.application.dto.PostSummaryResponse;
import com.dongneai.feed.domain.FeedErrorCode;
import com.dongneai.feed.domain.Post;
import com.dongneai.feed.domain.PostContent;
import com.dongneai.feed.domain.PostRepository;
import com.dongneai.feed.domain.Reaction;
import com.dongneai.feed.domain.ReactionRepository;
import com.dongneai.feed.infrastructure.PostQueryRepository;
import com.dongneai.place.api.PlaceApi;
import com.dongneai.region.api.RegionApi;
import com.dongneai.shared.exception.ApiException;
import com.dongneai.shared.exception.CommonErrorCode;
import com.dongneai.shared.response.CursorPage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FEED-02 글 상세, FEED-03 글 작성·수정·삭제, 공감, PLACE-02 '이 장소 이야기'. */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;
    private final ReactionRepository reactionRepository;
    private final FeedAssembler assembler;
    private final RegionApi regionApi;
    private final PlaceApi placeApi;

    /** 내 관심 동네에만 쓸 수 있다. 장소를 태그하면 장소의 '이 장소 이야기' 수가 늘어난다. */
    @Transactional
    public PostDetailResponse create(Long userId, PostRequest request) {
        if (request.regionId() == null) {
            throw new ApiException(CommonErrorCode.INVALID_REQUEST, "동네를 선택해 주세요.");
        }
        regionApi.requireOpenDong(request.regionId());
        regionApi.requireUserRegion(userId, request.regionId());
        PostContent content = toContent(request);
        if (content.placeId() != null) {
            placeApi.requireVisible(content.placeId());
        }
        Post post = postRepository.save(Post.write(userId, request.regionId(), content));
        if (post.getPlaceId() != null) {
            placeApi.applyPostChange(post.getPlaceId(), 1);
        }
        return toDetail(post, userId);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse get(Long viewerId, Long postId) {
        return toDetail(getVisible(postId), viewerId);
    }

    @Transactional
    public PostDetailResponse update(Long userId, Long postId, PostRequest request) {
        Post post = getOwned(userId, postId);
        PostContent content = toContent(request);
        Long oldPlaceId = post.getPlaceId();
        if (content.placeId() != null && !Objects.equals(content.placeId(), oldPlaceId)) {
            placeApi.requireVisible(content.placeId());
        }
        post.edit(content);
        postRepository.flush();
        if (!Objects.equals(oldPlaceId, post.getPlaceId())) {
            if (oldPlaceId != null) {
                placeApi.applyPostChange(oldPlaceId, -1);
            }
            if (post.getPlaceId() != null) {
                placeApi.applyPostChange(post.getPlaceId(), 1);
            }
        }
        return toDetail(post, userId);
    }

    @Transactional
    public void delete(Long userId, Long postId) {
        Post post = getOwned(userId, postId);
        post.delete();
        postRepository.flush();
        if (post.getPlaceId() != null) {
            placeApi.applyPostChange(post.getPlaceId(), -1);
        }
    }

    @Transactional(readOnly = true)
    public CursorPage<PostSummaryResponse> listByPlace(Long viewerId, Long placeId, String cursor, int size) {
        placeApi.requireVisible(placeId);
        List<Post> fetched = postQueryRepository.findByPlace(placeId, parseIdCursor(cursor), size + 1);
        CursorPage<Post> page = CursorPage.of(fetched, size, p -> String.valueOf(p.getId()));
        return new CursorPage<>(assembler.summaries(page.items(), viewerId), page.nextCursor());
    }

    /** 공감. 이미 공감했으면 그대로 (여러 번 눌러도 1). */
    @Transactional
    public LikeResponse like(Long userId, Long postId) {
        Post post = getVisible(postId);
        if (!reactionRepository.existsById(Reaction.postLikeId(userId, postId))) {
            reactionRepository.saveAndFlush(Reaction.likePost(userId, postId, LocalDateTime.now()));
            postRepository.addReactionCount(postId, 1);
            return new LikeResponse(true, post.getReactionCount() + 1);
        }
        return new LikeResponse(true, post.getReactionCount());
    }

    @Transactional
    public LikeResponse unlike(Long userId, Long postId) {
        Post post = getVisible(postId);
        if (reactionRepository.existsById(Reaction.postLikeId(userId, postId))) {
            reactionRepository.deleteById(Reaction.postLikeId(userId, postId));
            reactionRepository.flush();
            postRepository.addReactionCount(postId, -1);
            return new LikeResponse(false, Math.max(post.getReactionCount() - 1, 0));
        }
        return new LikeResponse(false, post.getReactionCount());
    }

    Post getVisible(Long postId) {
        return postRepository.findById(postId)
                .filter(Post::isVisible)
                .orElseThrow(() -> new ApiException(FeedErrorCode.POST_NOT_FOUND));
    }

    private Post getOwned(Long userId, Long postId) {
        Post post = getVisible(postId);
        if (!post.isWrittenBy(userId)) {
            throw new ApiException(CommonErrorCode.FORBIDDEN);
        }
        return post;
    }

    private PostDetailResponse toDetail(Post post, Long viewerId) {
        String nickname = assembler.nicknames(List.of(post.getUserId())).get(post.getUserId());
        var region = regionApi.findRegion(post.getRegionId())
                .map(r -> new PostDetailResponse.RegionRef(r.id(), r.name()))
                .orElse(null);
        var place = post.getPlaceId() == null ? null
                : FeedAssembler.tag(assembler.places(List.of(post.getPlaceId())).get(post.getPlaceId()));
        boolean liked = reactionRepository.existsById(Reaction.postLikeId(viewerId, post.getId()));
        return PostDetailResponse.of(post, new Author(post.getUserId(), nickname), region, place, liked, viewerId);
    }

    private static PostContent toContent(PostRequest request) {
        return new PostContent(request.category(), request.title(), request.content(), request.ageGroup(),
                request.placeId());
    }

    private static Long parseIdCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            throw new ApiException(CommonErrorCode.INVALID_REQUEST, "cursor 값이 올바르지 않습니다.");
        }
    }
}
