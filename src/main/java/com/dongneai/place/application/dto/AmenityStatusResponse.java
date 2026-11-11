package com.dongneai.place.application.dto;

import com.dongneai.place.domain.Amenity;
import com.dongneai.place.domain.AmenityStatus;

public record AmenityStatusResponse(Amenity amenity, AmenityStatus status, int confirmedCount) {
}
