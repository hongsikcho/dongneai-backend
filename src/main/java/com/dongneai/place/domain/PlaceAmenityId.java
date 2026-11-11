package com.dongneai.place.domain;

import com.dongneai.shared.code.Amenity;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PlaceAmenityId implements Serializable {
    private Long placeId;
    private Amenity amenity;
}
