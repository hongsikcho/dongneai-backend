package com.dongneai.place.application.dto;

import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;

public record AmenityStatusResponse(Amenity amenity, AmenityStatus status, int confirmedCount) {
}
