package com.dongneai.place.application;

import com.dongneai.place.api.PlaceApi;
import com.dongneai.place.domain.PlaceAmenity;
import com.dongneai.place.domain.PlaceAmenityRepository;
import com.dongneai.place.domain.PlaceErrorCode;
import com.dongneai.place.domain.PlaceRepository;
import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.exception.ApiException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceApiService implements PlaceApi {

    private final PlaceRepository placeRepository;
    private final PlaceAmenityRepository amenityRepository;

    @Override
    @Transactional(readOnly = true)
    public void requireVisible(Long placeId) {
        boolean visible = placeRepository.findById(placeId).filter(p -> p.isVisible()).isPresent();
        if (!visible) {
            throw new ApiException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void applyReviewChange(Long placeId, int countDelta, int ratingDelta) {
        if (countDelta == 0 && ratingDelta == 0) {
            return;
        }
        placeRepository.applyReviewChange(placeId, countDelta, ratingDelta);
    }

    @Override
    @Transactional
    public void applyAmenityVotes(Long placeId, Map<Amenity, AmenityVote> votes) {
        LocalDateTime now = LocalDateTime.now();
        Map<Amenity, PlaceAmenity> rows = amenityRepository.findByPlaceId(placeId).stream()
                .collect(Collectors.toMap(PlaceAmenity::getAmenity, Function.identity()));
        for (Amenity amenity : Amenity.values()) {
            AmenityVote vote = votes.getOrDefault(amenity, AmenityVote.NONE);
            PlaceAmenity row = rows.get(amenity);
            if (row != null) {
                row.applyVotes(vote.yes(), vote.no(), now);
            } else if (vote.yes() > 0 || vote.no() > 0) {
                amenityRepository.save(PlaceAmenity.byVotes(placeId, amenity, vote.yes(), vote.no(), now));
            }
        }
    }
}
