package com.dongneai.region.application.dto;

import com.dongneai.region.domain.Region;

public record UserRegionResponse(Long id, String name, String fullName, boolean primary) {

    public static UserRegionResponse of(Region region, boolean primary) {
        return new UserRegionResponse(region.getId(), region.getName(), region.getFullName(), primary);
    }
}
