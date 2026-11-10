package com.dongneai.place.domain;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceAmenityRepository extends JpaRepository<PlaceAmenity, PlaceAmenityId> {

    List<PlaceAmenity> findByPlaceId(Long placeId);

    List<PlaceAmenity> findByPlaceIdIn(Collection<Long> placeIds);
}
