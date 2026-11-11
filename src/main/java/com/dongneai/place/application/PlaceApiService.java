package com.dongneai.place.application;

import com.dongneai.place.api.PlaceApi;
import com.dongneai.place.domain.PlaceErrorCode;
import com.dongneai.place.domain.PlaceRepository;
import com.dongneai.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceApiService implements PlaceApi {

    private final PlaceRepository placeRepository;

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
}
